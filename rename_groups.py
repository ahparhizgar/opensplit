import os

root_dir = "."
exclude_dirs = {".git", ".gradle", ".idea", "build"}

replacements = {
    "Households": "Groups",
    "households": "groups",
    "Household": "Group",
    "household": "group"
}

def process_name(name):
    new_name = name
    for old, new in replacements.items():
        new_name = new_name.replace(old, new)
    return new_name

for dirpath, dirnames, filenames in os.walk(root_dir, topdown=False):
    # Skip excluded directories
    parts = dirpath.split(os.sep)
    if any(ex in parts for ex in exclude_dirs):
        continue

    for filename in filenames:
        new_name = process_name(filename)
        if new_name != filename:
            old_path = os.path.join(dirpath, filename)
            new_path = os.path.join(dirpath, new_name)
            print(f"Renaming file: {old_path} -> {new_path}")
            os.rename(old_path, new_path)

    for dirname in dirnames:
        # Also skip renaming the excluded directories themselves
        if dirname in exclude_dirs:
            continue

        new_name = process_name(dirname)
        if new_name != dirname:
            old_path = os.path.join(dirpath, dirname)
            new_path = os.path.join(dirpath, new_name)
            print(f"Renaming dir: {old_path} -> {new_path}")
            os.rename(old_path, new_path)
