/*
 * Dotan Beck 313602641
 * ex1.c
 */

#include <sys/types.h>
#include <sys/stat.h>

#include <fcntl.h>
#include <getopt.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#define MAX_BUFFER_SIZE 65536
#define DESTINATION_FILE_MODE S_IRUSR|S_IWUSR|S_IRGRP|S_IROTH

extern int opterr, optind;

void exit_with_usage(const char *message) {
	fprintf (stderr, "%s\n", message);
	fprintf (stderr, "Usage:\n\tex1 [-f] BUFFER_SIZE SOURCE DEST\n");
	exit(EXIT_FAILURE);
}

off_t read_stream_reverse(FILE *stream, char *buf, size_t count) {
	/*
		Reads from stream in reverse order into buf 'count' bytes.
	*/
	long original_position = 0;
	long cur_position = 0;
	long end_position = 0;
	
	original_position = ftell(stream);
	if (original_position == 0) {
		return 0;
	}
	end_position = original_position - count;
	if (end_position < 0) {
		end_position = -1;
	}
	
	cur_position = original_position;
	while (cur_position > end_position) {
		buf[original_position - cur_position] = fgetc(stream);
		cur_position--;
		fseek(stream, cur_position, SEEK_SET);
	}

	if (cur_position <= 0) {
		fseek(stream, 0, SEEK_SET);
	}

	return original_position - cur_position;
}


void append_file_reverse(const char *source_file, const char *dest_file, int buffer_size, int max_size, int force_flag) {
	/*
	 * Append source_file content to dest_file, buffer_size bytes at a time, in reverse order (start from the end of 'source_file').
	 * If source_file is larger than 'max_size', then only 'max_size' bytes will be written.
	 * NOTE: If max_size is smaller or equal to 0, then max_size should be IGNORED.
	 * If force_flag is true, then also create dest_file if does not exist. Otherwise print error, and exit.
	 *
	 * Examples:
	 * 1. if source_file's content is 'ABCDEFGH', 'max_size' is 3 and dest_file doesnt exist then
	 *	  dest_file's content will be 'HGF' (3 bytes from the end, in reverse order)
	 * 2. if source_file's content is '123456789', 'max_size' is -1, and dest_file's content is: 'HGF' then
	 *	  dest file's content will be 'HGF987654321'
	 *
	 * TODO:
	 * 	1. Open source_file for reading
	 *	2. Reposition the read file offset of source_file to the end of file (minus 1).
	 * 	3. Open dest_file for writing (Hint: is force_flag true?)
	 * 	4. Loop read_reverse from source and writing to the destination buffer_size bytes each time
	 * 	5. Close source_file and dest_file
	 *
	 *  ALWAYS check the return values of syscalls for errors!
	 *  If an error was found, use perror(3) to print it with a message, and then exit(EXIT_FAILURE)
	 */

	File *scrfp;
	//open stream and check its ready
	fp = open(source_file, O_WRONLY);
	if (fp == -1){
		perror("Unable to open source file for reading");
		return 0;
	}

	//pointing to the end of the file and gives the size of file. 
	int file_length = lseek(srcfp, 0, SEEK_END)


	//tring to open writing file. 
	File* dstfp = open(dest_file, O_WRONLY);
	//if that didnt work, if fource_flag is on, open one. else error. 
	if (dstfp == -1){
		if (force_flag == 1){
			dstfp = open (dest_file, O_WRONLY | O_CREAT , 0777);
			if (dstfp == -1){
				if(close(srcfp) == -1){
					perror("couldn't close source file");
                    exit(EXIT_FAILURE);
				}
				perror("Unable to open destination for writing");
                exit(EXIT_FAILURE);
			}
		}else{
			if(close(srcfp) == -1){
				perror("couldn't close source file");
                exit(EXIT_FAILURE);
			}
			perror("Unable to open destination for writing");
            exit(EXIT_FAILURE);
		}
	}
	//creating buffer. 
	char buffer[MAX_BUFFER_SIZE] = "";

	//loop
	int count = 0;
	//if max_size <= 0 ignoring him.  
	if (max_size <= 0  ){
		max_size = file_length;
	}

	while (count < max_size && count < file_length){
		read_stream_reverse(srcfp, buffer, buffer_size);
		c = write(destfp, buffer, buffer_size);
		if (c == -1){
			if(close(srcfp) == -1){
				perror("couldn't close source file");
                exit(EXIT_FAILURE);
			}
			if(close(destfp) == -1){
				perror("couldn't close source file");
                exit(EXIT_FAILURE);
			}
			perror("Unable to append buffer content to destination file");
            exit(EXIT_FAILURE);	
		}
	}

	//closing.
	free(buffer);
	if(close(srcfp) == -1){
		perror("couldn't close source file");
        exit(EXIT_FAILURE);
	}
	if(close(destfp) == -1){
		perror("couldn't close source file");
        exit(EXIT_FAILURE);
	}

	//finish
	printf("Content from file %s was successfully appended
	 (in reverse) to %s\n", source_file, dest_file);
	exit(EXIT_SUCCESS);






}

void parse_arguments(
		int argc, char **argv,
		char **source_file, char **dest_file, int *buffer_size, int *force_flag) {
	/*
	 * parses command line arguments and set the arguments required for append_file
	 */

	int option_character;

	opterr = 0; /* Prevent getopt() from printing an error message to stderr */

	while ((option_character = getopt(argc, argv, "f")) != -1) {
		switch (option_character) {
		case 'f':
			*force_flag = 1;
			break;
		default:  /* '?' */
			exit_with_usage("Unknown option specified");
		}
	}

	if (argc - optind != 3) {
		exit_with_usage("Invalid number of arguments");
	} else {
		*source_file = argv[argc-2];
		*dest_file = argv[argc-1];
		*buffer_size = atoi(argv[argc-3]);

		if (strlen(*source_file) == 0 || strlen(*dest_file) == 0) {
			exit_with_usage("Invalid source / destination file name");
		} else if (*buffer_size < 1 || *buffer_size > MAX_BUFFER_SIZE) {
			exit_with_usage("Invalid buffer size");
		}
	}
}

int main(int argc, char **argv) {
	int force_flag = 0; /* force flag default: false */
	char *source_file = NULL;
	char *dest_file = NULL;
	int buffer_size = MAX_BUFFER_SIZE;

	parse_arguments(argc, argv, &source_file, &dest_file, &buffer_size, &force_flag);

	append_file_reverse(source_file, dest_file, buffer_size, 0, force_flag);

	return EXIT_SUCCESS;
}

