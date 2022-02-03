package switcher;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Switcher {
    private final TrayIcon trayIcon;
    private List<CheckboxMenuItem> items;

    public Switcher() {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit()
                .createImage(getClass().getResource("/logo4.png"));

        trayIcon = new TrayIcon(image, "Environment switcher.Switcher");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Environment switcher.Switcher");
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void initMenu(List<Profile> profiles) {
        PopupMenu popup = new PopupMenu();

        items = profiles.stream().map(profile -> {
            CheckboxMenuItem item = new CheckboxMenuItem(profile.title);
            item.addItemListener(e -> {
                activate(profile, item);
            });
            popup.add(item);
            return item;
        }).collect(Collectors.toList());

        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(e -> {
            System.exit(0);
        });

        popup.add(exit);

        trayIcon.setPopupMenu(popup);

        if (!profiles.isEmpty()) {
            activate(profiles.get(0), items.get(0));
        }
    }

    void activate(Profile profile, CheckboxMenuItem item) {
        items.forEach(i -> i.setState(false));
        item.setState(true);

        System.out.println(profile.script);

        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", profile.script);
            Process p = pb.start();
            trayIcon.displayMessage(profile.title, "Activated " + profile.title + " profile", TrayIcon.MessageType.INFO);
        } catch (IOException e) {
            trayIcon.displayMessage(profile.title, e.getLocalizedMessage(), TrayIcon.MessageType.ERROR);
        }

    }

    public static void main(String[] args) {
        Switcher switcher = new Switcher();

        if ((args.length == 0) || (args.length % 2 != 0)) {
            switcher.trayIcon.displayMessage("Env switcher.Switcher", "No profiles are configured correctly", TrayIcon.MessageType.WARNING);
            return;
        }

        LinkedList<Profile> profiles = new LinkedList<>();

        int half = args.length / 2;
        for (int i = 0; i < half; i++) {
            profiles.add(new Profile(args[i * 2], args[i * 2 + 1]));
        }

        switcher.initMenu(profiles);
    }

    public static final class Profile {
        String title;
        String script;

        public Profile(String title, String script) {
            this.title = title;
            this.script = script;
        }
    }
}
