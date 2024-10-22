import os

# Define the directory you want to scan
root_dir = 'app'
output_file = 'folder_structure.txt'

def scan_directory(directory, indent_level=0):
    """Recursively scans the directory and returns the structure in markdown format"""
    markdown_output = ''
    indent = '    ' * indent_level
    try:
        # List directory contents
        for item in os.listdir(directory):
            item_path = os.path.join(directory, item)
            if os.path.isdir(item_path):
                # If it's a directory, append folder name and recurse
                markdown_output += f'{indent}- **{item}/**\n'
                markdown_output += scan_directory(item_path, indent_level + 1)
            else:
                # If it's a file, append file name and add its content
                markdown_output += f'{indent}- {item}\n'
                markdown_output += extract_file_content(item_path, indent_level + 1)
    except PermissionError:
        # Handle permissions error for certain directories
        pass
    return markdown_output

def extract_file_content(file_path, indent_level=0):
    """Reads the file content and formats it in markdown"""
    content_output = ''
    indent = '    ' * indent_level
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            content_output += f'{indent}```\n'
            for line in file:
                content_output += f'{indent}{line}'
            content_output += f'{indent}```\n'
    except Exception as e:
        content_output += f'{indent}Error reading file: {e}\n'
    
    return content_output

def create_markdown():
    """Creates markdown and writes it to a text file"""
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(f"# Folder structure for `{root_dir}`\n\n")
        folder_structure = scan_directory(root_dir)
        f.write(folder_structure)

if __name__ == "__main__":
    create_markdown()
    print(f"Markdown file '{output_file}' created successfully.")
