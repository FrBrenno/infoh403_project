from enum import Enum
import csv
import datetime
from pprint import pprint
import sys


if sys.platform == "win32":
    # Code to be executed if the user is using Windows
    first_out_filename = ".\\playground\\out\\first_computation_log.txt"
    follow_out_filename = ".\\playground\\out\\follow_computation_log.txt"
    ll1_log_filename = f".\\playground\\out\\ll1_check.txt"
    first_csv_name = f".\\playground\\out\\first.csv"
    follow_csv_name = f".\\playground\\out\\follow.csv"
    action_csv_name = f".\\playground\\out\\action.csv"

else:
    # Code to be executed if the user is using macOS or Linux
    first_out_filename = "./playground/out/first_computation_log.txt"
    follow_out_filename = "./playground/out/follow_computation_log.txt"
    ll1_log_filename = f"./playground/out/ll1_check.txt"
    first_csv_name = f"./playground/out/first.csv"
    follow_csv_name = f"./playground/out/follow.csv"
    action_csv_name = f"./playground/out/action.csv"
    log_file_name = f"./playground/out/ll1_check.log"


### Enumerations Terminals and Variables
class Variables(Enum):
    PROGRAM = "<Program>",
    CODE = "<Code>",
    INSTLIST = "<InstList>",
    INSTTAIL = "<InstTail>",
    INSTRUCTION = "<Instruction>",
    ASSIGN = "<Assign>",
    EXPRARITH = "<ExprArith>",
    EXPRARITHPRIME = "<ExprArith'>",
    PROD = "<Prod>",
    PRODPRIME = "<Prod'>",
    ATOM = "<Atom>",
    IF = "<If>",
    ELSETAIL = "<ElseTail>",
    COND = "<Cond>",
    CONDPRIME = "<Cond'>",
    AND = "<And>",
    ANDPRIME = "<And'>",
    CONDATOM = "<CondAtom>",
    COMP = "<Comp>"


class Terminals(Enum):
    VARNAME = "[VarName]",
    NUMBER = "[Number]",
    BEGIN = "begin",
    END = "end",
    DOTS = "...",
    ASSIGN = ":=",
    LPAREN = "(",
    RPAREN = ")",
    MINUS = "-",
    PLUS = "+",
    TIMES = "*",
    DIVIDE = "/",
    IF = "if",
    THEN = "then",
    ELSE = "else",
    AND = "and",
    OR = "or",
    LBRACK = "{",
    RBRACK = "}",
    EQUAL = "=",
    SMALLER = "<",
    WHILE = "while",
    DO = "do",
    PRINT = "print",
    READ = "read",
    EPSILON = "eps",
    EOF = "$"

### Grammar definition

grammar = {
           Variables.PROGRAM: [
               [Terminals.BEGIN, Variables.CODE, Terminals.END]
               ],
           Variables.CODE : [
               [Variables.INSTLIST],
               [Terminals.EPSILON]
               ],
           Variables.INSTLIST : [
               [Variables.INSTRUCTION, Variables.INSTTAIL]
               ],
           Variables.INSTTAIL : [
               [Terminals.DOTS, Variables.INSTLIST],
               [Terminals.EPSILON]
               ],
            Variables.INSTRUCTION : [
                [Variables.ASSIGN],
                [Variables.IF],
                [Terminals.WHILE, Variables.COND, Terminals.DO, Variables.INSTRUCTION],
                [Terminals.PRINT, Terminals.LPAREN, Terminals.VARNAME, Terminals.RPAREN],
                [Terminals.READ, Terminals.LPAREN, Terminals.VARNAME, Terminals.RPAREN],
                [Terminals.BEGIN, Variables.INSTLIST, Terminals.END]
                ],
            Variables.ASSIGN : [
                [Terminals.VARNAME, Terminals.ASSIGN, Variables.EXPRARITH]
                ],
            Variables.EXPRARITH : [
                [Variables.PROD, Variables.EXPRARITHPRIME]
                ],
            Variables.EXPRARITHPRIME : [
                [Terminals.PLUS, Variables.PROD, Variables.EXPRARITHPRIME],
                [Terminals.MINUS, Variables.PROD, Variables.EXPRARITHPRIME],
                [Terminals.EPSILON]
                ],
            Variables.PROD : [
                [Variables.ATOM, Variables.PRODPRIME]
                ],
            Variables.PRODPRIME : [
                [Terminals.TIMES, Variables.ATOM, Variables.PRODPRIME],
                [Terminals.DIVIDE, Variables.ATOM, Variables.PRODPRIME],
                [Terminals.EPSILON]
                ],
            Variables.ATOM : [
                [Terminals.VARNAME],
                [Terminals.NUMBER],
                [Terminals.LPAREN, Variables.EXPRARITH, Terminals.RPAREN],
                [Terminals.MINUS, Variables.ATOM]
                ],
            Variables.IF : [
                [Terminals.IF, Variables.COND, Terminals.THEN, Variables.INSTRUCTION, Terminals.ELSE, Variables.ELSETAIL]
                ],
            Variables.ELSETAIL : [
                [Variables.INSTRUCTION],
                [Terminals.EPSILON]
                ],
            Variables.COND : [
                [Variables.AND, Variables.CONDPRIME]
                ],
            Variables.CONDPRIME : [
                [Terminals.OR, Variables.AND, Variables.CONDPRIME],
                [Terminals.EPSILON]
                ],
            Variables.AND : [
                [Variables.CONDATOM, Variables.ANDPRIME]
                ],
            Variables.ANDPRIME : [
                [Terminals.AND, Variables.CONDATOM, Variables.ANDPRIME],
                [Terminals.EPSILON]
                ],
            Variables.CONDATOM : [
                [Terminals.LBRACK, Variables.COND, Terminals.RBRACK],
                [Variables.EXPRARITH, Variables.COMP, Variables.EXPRARITH]
                ],
            Variables.COMP : [
                [Terminals.EQUAL],
                [Terminals.SMALLER]
                ],
           }

### Variable Initialization
first_sets = {}
follow_sets = {}
action_table = {}

logging_first = []
logging_follow = []
logging_ll1 = []

### Functions

def getIndex(production, variable):
    """Returns a list of all indices of a given variable in a given production."""
    return [i for i, x in enumerate(production) if x == variable]
    

def compute_first(variable):
    """
    This function computes the first set of a given variable in the grammar.
    """
    logging_first.append(f"(INFO): Calculating first set for variable {variable}")
    # Base case: first set is already computed
    if first_sets.get(variable) is not None:
        logging_first.append(f"(DEBUG): Base case: First set for {variable} already computed: {first_sets[variable]}")
        return first_sets[variable]

    first_sets[variable] = set()

    # Base case: variable is a terminal
    if variable in Terminals:
        first_sets[variable].add(variable)
        logging_first.append(f"(DEBUG): Base case: Found terminal {variable} in variable {variable}")
        return first_sets[variable]

    for production in grammar[variable]:
        first_symbol = production[0]
        if first_symbol in Terminals:
            logging_first.append(f"(DEBUG): First Symbol of production {production} is terminal {first_symbol}")
            first_sets[variable].add(first_symbol)
        else:
            logging_first.append(f"(DEBUG): First Symbol of production {production} is variable {first_symbol}")
            first_sets[variable] = first_sets[variable].union(compute_first(first_symbol))
            if Terminals.EPSILON in compute_first(first_symbol):
                logging_first.append(f"(DEBUG): First Symbol of production {production} is variable {first_symbol} and epsilon is in first set of {first_symbol}")
                first_sets[variable] = first_sets[variable].union(compute_first(production[1]))

    logging_first.append(f"(INFO):Computed first set for variable {variable}: {first_sets[variable]}")
    logging_first.append("\n")
    return first_sets[variable]


def compute_follow(variable):
    """
    This function computes the follow set of a given variable in the grammar.
    It avoids infinite recursion by keeping track of the variables for which the follow set is already computed.
    """
    logging_follow.append(f"(INFO):## BEGIN Calculating follow set for variable {variable}")
    # Base case: follow set is already computed
    if follow_sets.get(variable) is not None:
        logging_follow.append(f"(DEBUG): Base case: Follow set for {variable} already computed: {follow_sets[variable]}")
        return follow_sets[variable]

    follow_sets[variable] = set()

    # Base case: variable is the start symbol
    if variable == Variables.PROGRAM:
        logging_follow.append(f"(DEBUG): Base case: Found start symbol {variable}")
        follow_sets[variable].add(Terminals.EOF)

    for key in grammar:
        logging_follow.append(f"(DEBUG): ### Checking variable {key}")
        for production in grammar[key]:
            if variable in production:
                variable_index_list = getIndex(production, variable) 
                for variable_index in variable_index_list:
                    if variable_index == len(production) - 1:
                        # Case 1: variable is at the end of the production
                        if key != variable:
                            logging_follow.append(f"(DEBUG): Case 1: Found variable {variable} at the end of production {production}")
                            follow_sets[variable] = follow_sets[variable].union(compute_follow(key))
                    else:
                        # Case 2: variable is not at the end of the production
                        next_symbol = production[variable_index + 1]
                        if next_symbol in Terminals:
                            # Case 2.1: next symbol is a terminal
                            logging_follow.append(f"(DEBUG): Case 2.1: Found terminal {next_symbol} after variable {variable}")
                            follow_sets[variable].add(next_symbol)
                        else:
                            # Case 2.2: next symbol is a variable
                            if Terminals.EPSILON in compute_first(next_symbol):
                                # Case 2.2.1: epsilon is in the first set of the next symbol
                                logging_follow.append(f"(DEBUG): Case 2.2.1: Found variable {next_symbol} after variable {variable} and epsilon is in first set of {next_symbol}")
                                follow_sets[variable] = follow_sets[variable].union(compute_first(next_symbol).difference({Terminals.EPSILON}))
                                logging_follow.append(f"(DEBUG): Case 2.2.1: Consider applying rule with epsilon in the next iteration")
                                follow_sets[variable] = follow_sets[variable].union(compute_follow(next_symbol))
                                if variable_index + 1 == len(production) - 1:
                                    logging_follow.append(f"(DEBUG): Case 2.2.1: Found variable {next_symbol} at the end of production {production}")
                                    follow_sets[variable] = follow_sets[variable].union(compute_follow(key))
                            else:
                                # Case 2.2.2: epsilon is not in the first set of the next symbol
                                logging_follow.append(f"(DEBUG): Case 2.2.2: Found variable {next_symbol} after variable {variable} and epsilon is not in first set of {next_symbol}")
                                follow_sets[variable] = follow_sets[variable].union(compute_first(next_symbol))

    logging_follow.append(f"(INFO):## END follow set for variable {variable}: {follow_sets[variable]}")
    logging_follow.append("\n")
    return follow_sets[variable]


def compute_action_table():
    """ 
    This function computes the action table of this grammar. 
    It gives the absolute number of the rule generating the terminal in the cell corresponding to the variable and the terminal.
    Absolute number means the number of the rule in the grammar, not the number of the rule in the production.
    """
    
    # Initialize action table
    for variable in Variables:
        action_table[variable] = {}
        for terminal in Terminals:
            action_table[variable][terminal] = None

    # Fill action table
    production_number = 1
    for variable in grammar:
        for production in grammar[variable]:
            first_set = compute_first(production[0]) 
            for terminal in first_set:
                if terminal != Terminals.EPSILON: 
                    action_table[variable][terminal] = production_number
            if Terminals.EPSILON in first_set: 
                follow_set = compute_follow(variable)
                for terminal in follow_set:
                    action_table[variable][terminal] = production_number
            production_number += 1
    return action_table

def check_ll1():
    cond1 = True
    cond2 = True
    cond3 = True
    is_ll1 = True

    # Check condition 1: No common prefix
    logging_ll1.append(f"#"*20+" CONDITION 1 "+"#"*20)
    logging_ll1.append("Checking condition 1: No common prefix")
    for non_terminal in grammar:
        logging_ll1.append("Checking non-terminal " + non_terminal.name)
        productions = grammar[non_terminal]
        prefixes = set()
        for production in productions:
            prefix = production[0]
            if prefix in prefixes:
                cond1 = False
                logging_ll1.append(f"Error: Common prefix '{prefix}' found in productions of {non_terminal}")
            prefixes.add(prefix)
    logging_ll1.append("Condition 1: No common prefix check completed")
    
    if not cond1:
        logging_ll1.append("Condition 1: No common prefix check failed")
        is_ll1 = False
    else:
        logging_ll1.append("Condition 1: PASSED!")

    # Check condition 2: No ε-productions conflict
    logging_ll1.append(f"#"*20+" CONDITION 2 "+"#"*20)
    logging_ll1.append("Checking condition 2: No ε-productions conflict")
    for non_terminal in grammar:
        logging_ll1.append("Checking non-terminal " + non_terminal.name + " for ε-productions")
        productions = grammar[non_terminal]
        has_epsilon = any([] in production for production in productions)
        if has_epsilon:
            follow_set = compute_follow(non_terminal)
            for terminal in follow_set:
                for production in productions:
                    if production and production[0] == terminal:
                        cond2 = False
                        logging_ll1.append(f"Error: Conflict found for ε-production of {non_terminal} and terminal '{terminal}'")
    logging_ll1.append("Condition 2: No ε-productions conflict check completed")
    
    if not cond2:
        logging_ll1.append("Condition 2: No ε-productions conflict check failed")
        is_ll1 = False
    else:
        logging_ll1.append("Condition 2: PASSED!")
        

    # Check if grammar is Strong LL(1) using first and follow set already computed
    logging_ll1.append(f"#"*20+" CONDITION bis "+"#"*20)
    logging_ll1.append("Checking if grammar is LL(1) using First and Follow sets")
    for variable in grammar:
        # Check if there is more than one production for the non-terminal
        nb_rules = len(grammar[variable])
        if nb_rules > 1:
            logging_ll1.append(f"### Checking non-terminal {variable.name} for LL(1)")
            # For each pair of productions, check if the intersection of the first of the first production union the follow of the non-terminal and the first of the second production is empty
            for i in range(nb_rules):
                for j in range(i+1, nb_rules):
                    first1 = compute_first(grammar[variable][i][0])
                    follow1 = compute_follow(variable)
                    first2 = compute_first(grammar[variable][j][0])
                    logging_ll1.append(f"Checking rule: {variable.name} -> {grammar[variable][i][0]} | {grammar[variable][j][0]}")
                    if first1.intersection(follow1).intersection(first2):
                        cond3 = False
                        logging_ll1.append(f"Error: Intersection between First sets of productions and Follow set is not empty")
                    else:
                        logging_ll1.append(f"Intersection between First sets of productions and Follow set is empty")
        
    if not cond3:
        logging_ll1.append("Condition 3: No intersection between First sets of productions and Follow set check failed")
        is_ll1 = False
    else:
        logging_ll1.append("Condition 3: PASSED!")

    logging_ll1.append("#"*40)
        
    # Check if grammar is LL(1)
    if is_ll1:
        logging_ll1.append("Grammar is LL(1)")
    else:
        logging_ll1.append("Grammar is not LL(1)")  
    
### Output functions

def first_follow_csv():
    with open(first_csv_name, "w+", newline='') as f:
        writer = csv.writer(f, lineterminator='\n')
        writer.writerow(["Variable", "FIRST"])
        first_set = {}
        for key in grammar:
            first_set = compute_first(key)
            writer.writerow([key.name, first_set])

    with open(follow_csv_name, "w+", newline='') as f:
        writer = csv.writer(f, lineterminator='\n')
        writer.writerow(["Variable", "FOLLOW"])
        follow_set = {}
        for key in grammar:
            follow_set = compute_follow(key)
            writer.writerow([key.name, follow_set])
            

def action_table_to_csv():
    """ 
    This function writes the action table to a csv file. 
    """    
    with open(action_csv_name, "w+", newline='') as f:
        writer = csv.writer(f, lineterminator='\n')
        writer.writerow([""] + [terminal.name for terminal in Terminals])
        for key in action_table:
            writer.writerow([key.name] + [action_table[key][terminal] for terminal in Terminals])




def write_log_file(file_path, log_data):
    with open(file_path, 'w', encoding='utf-8') as file:
        file.write(log_data)

def main():
    
    first_follow_csv()
    compute_action_table()
    check_ll1()
    action_table_to_csv()
    # Write log files
    write_log_file(first_out_filename, "\n".join(logging_first))
    write_log_file(follow_out_filename, "\n".join(logging_follow))
    write_log_file(ll1_log_filename, "\n".join(logging_ll1))

        

if __name__ == "__main__":
    main()
        
