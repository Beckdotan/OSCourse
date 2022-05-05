import os
import filecmp
import time
import subprocess
from subprocess import STDOUT, check_output

'''
    ./check_hw.py
    ./ex1.c
    ./tests/  -> test1.in, test1.out, test2.in, test2.out
    ./aux_files/ -> a.txt b.txt ...

    OUTPUT: result.txt -> Test0:0/1\nTest1:0/1
'''

SRC_CODE_NAME = 'ex1.c'
EXPECTED_OUTPUT_FILE = 'expected_dst_file'
EXE_NAME = '{}.out'.format(SRC_CODE_NAME.split('.')[0])

def output_result_file(results):
    for i in range(len(results)):
        print(f'Test{i}:{"Pass" if (results[i] > 0) else "Fail"}')

def append_src_to_dst(srcfile, dstfile):
    # opening file with correct permissions
    dst_exists = os.path.exists(dstfile)
    src_exists = os.path.exists(srcfile)
    
    if dst_exists:
        f1 = open(dstfile, 'r', encoding="utf8")
    if src_exists:
        f2 = open(srcfile, 'r', encoding="utf8")
    f3 = open(EXPECTED_OUTPUT_FILE, 'w+', encoding="utf8") 
    
    # appending the contents of the files to the expected file
    if dst_exists:
        f3.write(f1.read())
    if src_exists:
        f3.write(f2.read())
    
    # relocating the cursor of the files at the beginning 
    if dst_exists:
        f1.seek(0)
    if src_exists:
        f2.seek(0)
    
    # closing the files 
    if dst_exists:
        f1.close()
    if src_exists:
        f2.close()
    f3.close()
    
def is_identical_content(file1, file2):
    is_identical = True
    f1=open(file1,"r", encoding="utf8") 
    f2=open(file2,"r", encoding="utf8") 
    for line1 in f1:
        for line2 in f2:
            if line1!=line2: 
                is_identical = False
            break
    f1.close() 
    f2.close()
    return is_identical

def check_submission(timeout):
    test_file_names = os.listdir('tests/')
    in_test_files = [x for x in test_file_names if x.endswith('.in')]

    submission_file_items = os.listdir('.')
    if SRC_CODE_NAME not in submission_file_items:
        output_result_file([0]*len(in_test_files))
        print('Missing {}'.format(SRC_CODE_NAME))
        return

    os.system('gcc {src} -o {exe}'.format(src=SRC_CODE_NAME, exe=EXE_NAME))
    if EXE_NAME not in os.listdir('.'):
        output_result_file([0]*len(in_test_files))
        print('Code did not compile.')
        return

    results = []

    for i in range(1, len(in_test_files) + 1):
        
        # if an expected output file exists from prev. iteration, remove it
        if os.path.exists(EXPECTED_OUTPUT_FILE):
            os.remove(EXPECTED_OUTPUT_FILE)
            
        test_in = f'test{i}.in'
        test_name = test_in.split('.')[0]

        with open('tests/{}'.format(test_in)) as input_test_file:
            input_lines = input_test_file.readlines()
            should_succeed = True if 'V' in input_lines[0] else False
            input_line = input_lines[1] if (len(input_lines) > 1) else ''
        
        
        # create the expected output file
        input_line_parts = input_line.split()
        if len(input_line_parts) > 2:
            src_file = input_line_parts[-2]
            dst_file = input_line_parts[-1]
                
            append_src_to_dst(src_file, dst_file)

        execution_cmd = './{exe} {input}'.format(exe=EXE_NAME, input=input_line)
        

        out_file_name = 'tests/{}.out'.format(test_in.split('.')[0])
        with open(out_file_name) as output_test_file:
            output_lines = output_test_file.readlines()
            out_line = ''.join(output_lines)

        cmd_success_output = ''
        try:
            cmd_success_output = subprocess.check_output(execution_cmd, shell=True, timeout=timeout, stderr=subprocess.STDOUT).decode()
            if not should_succeed:
                results.append(0)
                continue
            
            # compare expected output to the program's output
            same_res = is_identical_content(EXPECTED_OUTPUT_FILE, dst_file)
            if not same_res:
                print('Test{}: The file\'s content after appending does not match the expected output'.format(i-1))
                results.append(0)
                continue

        except subprocess.TimeoutExpired:
            results.append(0)
            continue

        except subprocess.CalledProcessError as e:
            cmd_success_output = e.output.decode()
            if should_succeed:
                results.append(0)
                continue

        if out_line != cmd_success_output:
            print('Test{}: The console output does not match the expected output. \n {} \n instead of: \n {}'.format(i-1, cmd_success_output, out_line))
            results.append(0)
            continue

        results.append(1)
        

    output_result_file(results)



if __name__ == '__main__':
    timeout = 5
    check_submission(timeout)
