# Document-Search-Tool
The provided program is designed to search a set of documents for a specified search term or phrase, documenting the results in order of their relevance.

# Compilation & Execution
Following the downloading or cloning of the associated files, the program may be compiled and executed via one of the following methods.

The aforementioned methods are as follows:

    Commandline Compilation & Execution:
    
        1. Navigate to the source directory containing the Document Search Tool files.
        2. Compile the program using the following command:
            -$: javac DocumentSearchTool.java
        3. Execute the program using the following command:
            -$: java DocumentSearchTool
    
    IDE Compilation & Execution:
    
        1. Within the IDE of your choosing, import the Document Search Tool as a new Java Project.
        2. Once implemented, build the project with the corresponding command.
            - For example:
                IntelliJ - Build | Build Project
                Eclipse - Project | Build Working Set
        3. Execute the program using the corresponding run command.
            - For example:
                IntelliJ - Run | Run 'Document Search Tool'
                Eclipse - Run | Run
                
# General Interaction
The program at hand is a dynamic search tool that processing user input via the console and returns the corresponding results in the order of their relevance to the specified search criteria. As such, all interaction with the program will be conducted via the console / commandline.

The program implements the following search methodologies, and their corresponding features.

    1. String Match:
        Systematically iterate through a specified file's tokens, detecting and documenting occurrences of the provided search term to be later displayed to the user.
        
        - Features:
            
            1. Matching may be conducted by way of Exact Matching or Partial Matching per the user's specification.
            
                a. Exact Matching:
                    - Exact Matching will only detect and return occurrences of the full search term as provided by the user.
                    
                b. Partial Matching
                    - Partial Matching will detect all occurrences of the search term as provided by the user, regardless if it is a substring or singular word.
                    
            2. Matching may be conducted Case-Sensitively or Case-Insensitively per the user's specification.
            
    2. Regular Expression:
        Systematically iterate through a specified file's tokens, detecting and documenting occurrences of the provided regex to be later displayed to the user.
        
        - Features:
                    
            1. Matching may be conducted Case-Sensitively or Case-Insensitively per the user's specification.
            
    3. Indexed:
        Parse and tokenize the specified files into a file system simulating a low-level database in order to expedite the searching process. 
        
        - Features:
                    
            1. Matching may be conducted Case-Sensitively or Case-Insensitively per the user's specification.

# Testing Behavior
