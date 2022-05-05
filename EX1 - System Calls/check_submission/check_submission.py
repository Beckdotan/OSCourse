# This is an "envelope script": it makes sure that the submission file is valid. Also it runs the script "check_hw.py"
# inside a docker.

import sys
import os
from os import listdir
from os.path import isfile, join
import zipfile
import re
import subprocess
from subprocess import STDOUT, check_output

mypath = '.'
SRC_CODE_NAME = 'ex1.c'
PDF_NAME = 'ex1.pdf'

docker_installed_cmd = 'docker -v | echo $?'
docker_build_cmd = 'docker build . -t my_env --network=host >/dev/null'
docker_run_cmd = 'docker run my_env'


def check_submission(zip_filepath):
    
    #delete files from previous runs if existence
    if os.path.exists(SRC_CODE_NAME):
        os.remove(SRC_CODE_NAME)
    if os.path.exists(PDF_NAME):
        os.remove(PDF_NAME)
        
    # verify that the file exists
    if not isfile(zip_filepath):
        print('A submission file could not be found.')
        return
    
    # check if the name pattern is correct
    file_path, zip_filename = os.path.split(zip_filepath)
    if not re.match(r'ex1-\d\d\d\d\d\d\d\d\d.zip', zip_filename):
        print('Your submission file has invalid name pattern (should be ex1-YOUR_ID.zip) ')
        return

    # unzip file and check source code existence
    with zipfile.ZipFile(zip_filepath, 'r') as zip_ref:
        zip_ref.extractall(mypath)
    if not os.path.exists(SRC_CODE_NAME):
        print('Your submission does not include the source file ' + SRC_CODE_NAME)
        return
        
    if not os.path.exists(PDF_NAME):
        print('Your submission does not include the pdf file ' + PDF_NAME)
        return
        
    # check if docker is installed on current machine
    cmd_output = subprocess.check_output(docker_installed_cmd, shell=True, stderr=subprocess.STDOUT).decode()
    if cmd_output.strip() != '0':
        print('Docker is currently not installed on your machine. Please install it first.')
        return

    # build a docker for some other code-related tests
    print('A docker is being built. It might take a minute or two if this is the first time..')
    os.system(docker_build_cmd)


    print('Done. Smoke tests are running now..')
    cmd_output = subprocess.check_output(docker_run_cmd, shell=True, stderr=subprocess.STDOUT).decode()

    print(cmd_output)


if __name__ == '__main__':
    if len(sys.argv) != 2:
        print('You should provide a full path of your submission file as a command line argument')
    else:
        check_submission(sys.argv[1])
