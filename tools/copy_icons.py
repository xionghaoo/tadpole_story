from shutil import copyfile

import os

from os import listdir
from os.path import isfile, join


# SOURCE_DIR = '/Users/destiny/work/ubtech/蝌蚪项目/icons'
SOURCE_DIR = 'E:\\icons'
TARGET_DIR = '../app/src/main/res'
CONFIG_FILE = 'copy_config.txt'
# 将';'号左边的文件名修改成右边的名称
SPLIT_CHARACTER = ';'


def copy_file(source, target):
    # 1080p
    copy_file_dpi(source, target, 'xxhdpi', "hdpi")
    # 720p
    copy_file_dpi(source, target, 'xhdpi', "mdpi")
    # xh_source = join('{0}\\mipmap-xhdpi'.format(SOURCE_DIR), source)
    # xh_target_path = '{0}\\mipmap-xhdpi'.format(TARGET_DIR)
    # if not os.path.exists(xh_target_path):
    #     os.mkdir(xh_target_path)
    # xh_target = join(xh_target_path, target)
    #
    # if isfile(xh_source):
    #     copyfile(xh_source, xh_target)
    #
    # xxh_source = join('{0}\\mipmap-xxhdpi'.format(SOURCE_DIR), source)
    # xxh_target_path = '{0}\\mipmap-xxhdpi'.format(TARGET_DIR)
    # if not os.path.exists(xxh_target_path):
    #     os.mkdir(xxh_target_path)
    # xxh_target = join(xxh_target_path, target)
    # if isfile(xxh_source):
    #     copyfile(xxh_source, xxh_target)


def copy_file_dpi(source, target, source_dpi, target_dpi):
    dpi_source = join('{0}/mipmap-{1}'.format(SOURCE_DIR, source_dpi), source)
    target_path = '{0}/mipmap-{1}'.format(TARGET_DIR, target_dpi)
    if not os.path.exists(target_path):
        os.mkdir(target_path)
    dpi_target = join(target_path, target)

    if isfile(dpi_source):
        print('copy file {0} -> {1}'.format(dpi_source, dpi_target))
        copyfile(dpi_source, dpi_target)
    else:
        print('{} is not a file'.format(dpi_source))


def rename_all():
    print("-------copy start-----")
    for line in open(CONFIG_FILE, 'r', encoding='utf-8'):
        names = line.rstrip().split(SPLIT_CHARACTER)
        copy_file(names[0], names[1])
    print("-------copy end-----")


rename_all()

