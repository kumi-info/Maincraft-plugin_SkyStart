package local.skystart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SkyStart
 *
 * s2e-skyrunner プラグインが保存している startLocation を直接読み取り、
 * プレイヤーをスカイランナーのスタート地点へテレポートするコンパニオンプラグイン。
 *
 * - スタート位置は実行のたびに s2e-skyrunner/config.yml から読むため、
 *   /skyrunner create でスタートを引き直しても常に最新に追従する。
 * - 向き(yaw)は config.yml の directionX / directionZ から計算し、
 *   コースの進行方向を向くようにする。
 *
 * 使い方:
 *   /skystart            自分をTP
 *   /skystart <player>   指定プレイヤーをTP
 *   /skystart all        オンライン全員をTP
 */
public final class SkyStart extends JavaPlugin implements TabExecutor {

    // s2e-skyrunner のデータフォルダ名（plugins/ 直下）
    private static final String SKYRUNNER_DIR = "s2e-skyrunner";

    @Override
    public void onEnable() {
        if (getCommand("skystart") != null) {
            getCommand("skystart").setExecutor(this);
            getCommand("skystart").setTabCompleter(this);
        } else {
            getLogger().severe("plugin.yml に skystart コマンドが定義されていません。");
        }
        getLogger().info("SkyStart 有効化。スタート位置は s2e-skyrunner/config.yml を参照します。");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // スタート地点を取得
        Location start;
        try {
            start = loadStartLocation();
        } catch (IllegalStateException e) {
            sender.sendMessage("§c[SkyStart] " + e.getMessage());
            return true;
        }

        // 対象プレイヤーを決定
        List<Player> targets = new ArrayList<>();

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c[SkyStart] コンソールから実行する場合はプレイヤー名を指定してください。例: /skystart <player>");
                return true;
            }
            targets.add((Player) sender);
        } else if (args[0].equalsIgnoreCase("all")) {
            targets.addAll(Bukkit.getOnlinePlayers());
            if (targets.isEmpty()) {
                sender.sendMessage("§e[SkyStart] オンラインのプレイヤーがいません。");
                return true;
            }
        } else {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage("§c[SkyStart] プレイヤーが見つかりません: " + args[0]);
                return true;
            }
            targets.add(target);
        }

        int ok = 0;
        for (Player p : targets) {
            // teleport は同期スレッドから呼ぶ必要がある（コマンドはメインスレッドなのでそのままでOK）
            if (p.teleport(start)) {
                ok++;
                p.sendMessage("§a[SkyStart] スタート地点へテレポートしました。");
            } else {
                sender.sendMessage("§c[SkyStart] " + p.getName() + " のテレポートに失敗しました。");
            }
        }

        if (targets.size() > 1 || !(sender instanceof Player && targets.contains(sender))) {
            sender.sendMessage("§7[SkyStart] " + ok + "/" + targets.size() + " 人をスタート地点へTPしました ("
                    + fmt(start) + ")");
        }
        return true;
    }

    /**
     * s2e-skyrunner/config.yml から startLocation を読み取り Location を返す。
     * directionX/directionZ から進行方向の yaw を計算してセットする。
     */
    private Location loadStartLocation() {
        // plugins/SkyStart/ の親 = plugins/  → plugins/s2e-skyrunner/config.yml
        File skyrunnerCfg = new File(getDataFolder().getParentFile(), SKYRUNNER_DIR + File.separator + "config.yml");
        if (!skyrunnerCfg.isFile()) {
            throw new IllegalStateException("s2e-skyrunner の config.yml が見つかりません: " + skyrunnerCfg.getPath());
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(skyrunnerCfg);
        if (!cfg.isConfigurationSection("startLocation")) {
            throw new IllegalStateException("config.yml に startLocation がありません。先に /skyrunner create でゲームを作成してください。");
        }

        String worldName = cfg.getString("startLocation.world", "");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalStateException("ワールドが読み込まれていません: '" + worldName + "'");
        }

        double x = cfg.getDouble("startLocation.x");
        double y = cfg.getDouble("startLocation.y");
        double z = cfg.getDouble("startLocation.z");

        // 進行方向から yaw を算出（無ければ +X 方向）
        int dx = cfg.getInt("directionX", 1);
        int dz = cfg.getInt("directionZ", 0);
        float yaw = yawFromDirection(dx, dz);

        Location loc = new Location(world, x, y, z, yaw, 0.0f);
        if (getConfig().getBoolean("debug", false)) {
            getLogger().info("startLocation 読込: " + fmt(loc) + " (dir=" + dx + "," + dz + ")");
        }
        return loc;
    }

    /** 方向ベクトル(dx,dz)を Minecraft の yaw(度)へ変換。+X=-90, +Z=0, -X=90, -Z=180。 */
    private float yawFromDirection(int dx, int dz) {
        if (dx == 0 && dz == 0) {
            return -90.0f; // 既定: +X
        }
        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        return (float) yaw;
    }

    private String fmt(Location l) {
        return String.format("%s %.1f %.1f %.1f", l.getWorld() == null ? "?" : l.getWorld().getName(),
                l.getX(), l.getY(), l.getZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            if ("all".startsWith(prefix)) {
                out.add("all");
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) {
                    out.add(p.getName());
                }
            }
        }
        return out;
    }
}
