#init.py
import os
import sys
import argparse

def initialize():
    def project_name_type(string):
        if ' ' in string:
            raise argparse.ArgumentTypeError('%r contains spaces.' % string)
        if not string[0].isupper():
            raise argparse.ArgumentTypeError('%r is not in PascalCase.' % string)
        return string
    
    parser = argparse.ArgumentParser(description = 'Initializes your first SpatialOS Project!')
    parser.add_argument('projectname', help='The name of your project. Spaces are not allowed. Must be PascalCase.', type=project_name_type)
    parser.add_argument('organization', help='The name of your organisation. (Default: Improbable)', default='Improbable')
    args = parser.parse_args()
    rename_project(args.projectname, args.organization)
    
def rename_project(projectname, organization):
    package_organization = parse_for_package_name(organization)
    package_projectname = parse_for_package_name(projectname)
    
    package_folder = package_organization + '/' + package_projectname
    package_name = package_organization + '.' + package_projectname
    
    dpl_name = projectname.lower()
    
    # BuildConfiguration.scala
    def patch_build_config():
        alter_file('project/BuildConfiguration.scala', [
            string_replacer('projectName = "BlankProject"', 'projectName = "%s"' % projectname),
            string_replacer('organisation = "improbable"', 'organisation = "%s"' % organization),
            string_replacer('version = Versions.fetchVersion("blankproject")', 'version = Versions.fetchVersion("%s")' % projectname)
        ])
    try_to_patch('BuildConfiguration', patch_build_config)
        
    # Migration files
    def patch_migration():
        old_migration_file = 'Spec/src/main/scala/improbable/blankproject/migrations/ExampleMigration.scala'
        new_migration_file = 'Spec/src/main/scala/%s/migrations/ExampleMigration.scala' % package_folder
        
        plan_move_file(old_migration_file, new_migration_file)
        alter_file(old_migration_file, [
            string_replacer('package improbable.blankproject.migrations', 'package %s.migrations' % package_name),
            string_replacer('"improbable.blankproject.', '"%s.' % package_name)
        ])
        
        move_file(old_migration_file, new_migration_file)
    try_to_patch('Migration Files', patch_migration)
    
    # game.properties
    def patch_game_properties():
        alter_file('Gamelogic/src/main/resources/game.properties', 
            string_replacer('game.name = BlankProject', 'game.name = %s' % projectname)
        )
    try_to_patch('game.properties', patch_game_properties)
    
    # WorldApps
    def patch_world_app_list():
        old_world_app_list_file = 'Gamelogic/src/main/scala/improbable/apps/BlankProjectWorldAppList.scala'
        new_world_app_list_file = 'Gamelogic/src/main/scala/improbable/apps/%sWorldAppList.scala' % projectname
        
        plan_move_file(old_world_app_list_file, new_world_app_list_file)
        alter_file(old_world_app_list_file, 
            string_replacer('object BlankProjectWorldAppList {', 'object %sWorldAppList {' % projectname)
        )
        move_file(old_world_app_list_file, new_world_app_list_file)
    try_to_patch('WorldAppList', patch_world_app_list)
    
    # Launcher
    def patch_launcher():
        old_launcher_file = 'Gamelogic/src/main/scala/improbable/blankproject/launcher/SimulationLauncher.scala'
        new_launcher_file = 'Gamelogic/src/main/scala/%s/launcher/SimulationLauncher.scala' % package_folder
        
        old_launch_config_file = 'Gamelogic/src/main/scala/improbable/blankproject/launcher/SimulationLaunchConfig.scala'
        new_launch_config_file = 'Gamelogic/src/main/scala/%s/launcher/SimulationLaunchConfig.scala' % package_folder
        
        plan_move_file(old_launcher_file, new_launcher_file)
        plan_move_file(old_launch_config_file, new_launch_config_file)
        
        replacer = string_replacer('package improbable.blankproject.launcher', 'package %s.launcher' % package_name)
        alter_file(old_launcher_file, replacer)
        alter_file(old_launch_config_file, replacer)
        
        move_file(old_launcher_file, new_launcher_file)
        move_file(old_launch_config_file, new_launch_config_file)
    try_to_patch('Launcher and LaunchConfig', patch_launcher)
    
    # Bootstrap.cs
    def patch_bootstrap():
        alter_file('Engines/Unity/Editor/Assets/Bootstrap.cs',
            string_replacer('AppName = "blankproject"', 'AppName = "%s"' % dpl_name)
        )
    try_to_patch('Bootstrap.cs', patch_bootstrap)
    
    # example.dpl.json
    def patch_dpl():
        alter_file('example.dpl.json',
            string_replacer(
                '"-DCLOUD_LAUNCH_CONFIG": "improbable.blankproject.launcher.SimulationLaunchWithAutomaticEngineStartupConfig"',
                '"-DCLOUD_LAUNCH_CONFIG": "%s.launcher.SimulationLaunchWithAutomaticEngineStartupConfig"' % package_name
            )
        )
    try_to_patch('example.dpl.json', patch_dpl)
    
    print('Patching Complete. Please remember to change README.md')
    
class AlreadyPatchedException(Exception):
    pass
    
def try_to_patch(description, patch):
    try:
        patch()
    except AlreadyPatchedException as e:
        print('Could not patch %s. Already patched.' % description)

def move_file(oldfile, newfile):
    oldfile = pathsep(oldfile)
    newfile = pathsep(newfile)
    
    if not os.path.exists(parent_dir(newfile)):
        os.makedirs(parent_dir(newfile))
        
    os.rename(oldfile, newfile)
    try:
        clean_empty_dirs(oldfile)
    except WindowsError:
        print('Could not clean directory %s' % oldfile)
    
def alter_file(filename, string_operation):
    filename = pathsep(filename)
    file = open(filename, 'r')
    
    output = ''
    # Files are small enough to read into memory
    for line in file.readlines():
        if isinstance(string_operation, list):
            for operation in string_operation:
                line = operation(line)
            output += line
        else:
            output += string_operation(line)
    
    file.close()
    
    file = open(filename, 'w')
    file.write(output)
    file.close()
    
def plan_move_file(oldfile, newfile):
    if not os.path.isfile(oldfile) and os.path.isfile(newfile):
        raise AlreadyPatchedException()
    
def clean_empty_dirs(file):
    dir = parent_dir(file)
    if not os.listdir(dir):
        os.rmdir(dir)
        clean_empty_dirs(dir)
    
def parent_dir(file):
    return os.path.abspath(os.path.join(file, os.pardir))

def string_replacer(old, new):
    def replace_operation(input):
        return input.replace(old, new)
    return replace_operation
    
def parse_for_package_name(text):
    text.replace(' ', '')
    return text.lower()
    
def pathsep(path):
    return path.replace('/', os.path.sep)
    
initialize()