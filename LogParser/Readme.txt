=== How to compile the project, and what to run: ===

1) Git pull the project
2) Download and add the following libraries to your project build path
	- Guava 16
	- Apache Commons IO

3) Execute "LogParseDataWriter" - be careful of the flags on top:
	WRITE_OUTPUT - set this to true if you want to re-generate the ant_commits and ant_modified_files
	If running on *nix systems (including Mac), change the path to "out" from \\ to /

4) Runner is no longer valid - it is being kept there for historical reasons (do not run!)

=== For testings ===
1) Near the top of the class LogParseDataWriter, change the path for : String antLog = ...
   -> Change this to point to the "testlog.txt". 
   -> small_history.txt contains the commits we are interested in (i.e. commits which don't just change 
      the license, or modify the comments in all of the source files). 