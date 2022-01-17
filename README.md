# jcoem

This is the Java implementation of my Coem language. (Planning on a JavaScript implementation soon.) This was built by following the first half of Robert Nystrom's super accessible [_Crafting Interpreters_](https://craftinginterpreters.com/) and customising it according to my intentions for the language.

## Setup

If you have a setup for building Java applications, feel free to use that!

If you're new to Java build tools like I was, I found that the [Java build tools in VSCode](https://code.visualstudio.com/docs/java/java-build) are really easy to use. It scans for the `pom.xml` file in the repository and uses the information in that file to build the application. After installing these tools, you can run the program by clicking the "Run" button that appears above the `main()` function in `Coem.java`, or by clicking the run button at the top right of the window when you open `Coem.java`.

## Overview

### Files

- [`Coem.java`](https://github.com/coem-lang/jcoem/blob/master/src/main/java/com/ky/coem/Coem.java) takes an input, either one line of code in an interactive prompt or a file containing source code, and runs the code.
- [`Scanner.java`](https://github.com/coem-lang/jcoem/blob/master/src/main/java/com/ky/coem/Scanner.java) takes a source string and returns a List of `Token`s.
- [`TokenType.java`](https://github.com/coem-lang/jcoem/blob/master/src/main/java/com/ky/coem/TokenType.java) defines the `TokenType` enum, which are all the valid tokens in the grammar.
- [`Token.java`](https://github.com/coem-lang/jcoem/blob/master/src/main/java/com/ky/coem/Token.java) is a class that describes a `TokenType`, a lexeme string, a literal object, and a line number.
- [`Parser.java`](https://github.com/coem-lang/jcoem/blob/master/src/main/java/com/ky/coem/Parser.java) takes a List of `Token`s and returns a List of `Stmt`s.
- [`Interpreter.java`](https://github.com/coem-lang/jcoem/blob/master/src/main/java/com/ky/coem/Interpreter.java) takes a List of `Stmt`s and executes them. It also maintains different environments as required.
- [`Environment.java`](https://github.com/coem-lang/jcoem/blob/master/src/main/java/com/ky/coem/Environment.java) holds a Map of identifiers and their corresponding values.
- [`Stmt.java`](https://github.com/coem-lang/jcoem/blob/master/src/main/java/com/ky/coem/Stmt.java) describes different types of statements.
- [`Expr.java`](https://github.com/coem-lang/jcoem/blob/master/src/main/java/com/ky/coem/Expr.java) describes different types of expressions.

### Flow

This is a rough explanation for how the program works, as I currently understand it. There wil probably be some misunderstandings in here, as I'm still learning!

1. **`Coem.java`**

The entry point for the program is `Coem.java`. Running this file from the command line without any arguments starts an interactive prompt, or [REPL](http://www.craftinginterpreters.com/scanning.html) ("Read a line of input, Evaluate it, Print the result, then Loop and do it all over again"). This lets you run code one line at a time. Alternatively, if you run the file followed by a path to a file, it'll run the code in that file.

2. **`Scanner.java`**

Assuming you give it a file, it'll take a string of the contents of the file, and create a new `Scanner` object with that source string. Before the `Scanner` does any scanning, we can notice that it has already defined a HashMap of keywords, which maps the strings of keywords to the `TokenType` enum, as defined in `TokenType.java`, which will be relevant as it starts scanning. Back in `Coem.java`, we take the newly created `Scanner` and call the `scanTokens()` function which we expect to return a List of `Token`s.

The `scanTokens()` advances through each character of the source and adds `Token`s to a List, keeping track of position and line number.
- For most individual characters, it runs through a switch statement and adds a `Token` according to the the corresponding `TokenType` enum.
- When a comment character is encountered, it ignores everything until a new line is started, or until the file ends.
- Whitespace such as spaces, carriage returns, and tabs are ignored, but newlines cause the line number to increment. 
- When a quotation mark is encountered, it takes everything until the next quotation mark.
- For everything else, it assumes it's an identifier. It takes all the following valid identifier characters, then checks if it's a defined keyword. If it's not in the keywords, it's an identifier, meaning a user-defined variable or function name.
At the end of the source, an EOF (end of file) `Token` is added, and the List of `Token`s is returned.

3. **`Parser.java`**

Now that we have a List of all the `Token`s in the source, we pass that into a new `Parser` object. Here, tokens are matched to different types of statements, eventually building up a List of `Stmt`s. Back in `Coem.java`, this List is passed into the `Interpreter`.

4. **`Interpreter.java`**

The `Interpreter` executes each statement in the List given. It also keeps track of environments as required, which holds a Map of identifiers and their corresponding values.