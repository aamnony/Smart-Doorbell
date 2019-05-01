import subprocess
import time

import gi
gi.require_version('Wnck','3.0')
from gi.repository import Wnck


def open_chromium():
    # https://stackoverflow.com/questions/45426203/minimize-window-with-python
    chromium = subprocess.Popen(['chromium-browser', '--window-size=10,10', 'https://www.smartdoorbell.ga/room1'], shell=False)

    screen = Wnck.Screen.get_default()
    time.sleep(6)
    screen.force_update()
    windows = screen.get_windows()
    for w in windows:
        #if 'chromium' in w.get_name().lower():
        if chromium.pid == w.get_pid():
            w.minimize()
            return

# time.sleep(20)
# chromium.kill()


open_chromium()
