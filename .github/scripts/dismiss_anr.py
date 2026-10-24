import re
import subprocess
import xml.etree.ElementTree as ET

try:
    root = ET.parse("out/window.xml").getroot()
except Exception:
    raise SystemExit(0)

for node in root.iter("node"):
    if node.get("text") == "Wait":
        bounds = re.findall(r"\d+", node.get("bounds", ""))
        if len(bounds) == 4:
            x = (int(bounds[0]) + int(bounds[2])) // 2
            y = (int(bounds[1]) + int(bounds[3])) // 2
            subprocess.run(
                ["adb", "shell", "input", "tap", str(x), str(y)],
                check=False,
            )
        break
