import os
import re

version = os.getenv("VERSION")


def modify_project():
	with open("gradle.properties", "r") as f:
		data = f.read()

	data = re.sub(r"mod_version=.+", f"mod_version={version}", data)

	with open("gradle.properties", "w") as f:
		f.write(data)

def modify_main():
	with open("src/main/java/net/minusmc/minusbounce/MinusBounce.kt", "r") as f:
		data = f.read()

	data = re.sub(r"const val CLIENT_VERSION = .+", f"const val CLIENT_VERSION = \"{version}\"", data)

	with open("src/main/java/net/minusmc/minusbounce/MinusBounce.kt", "w") as f:
		f.write(data)

if __name__ == "__main__":
	modify_project()
	modify_main()