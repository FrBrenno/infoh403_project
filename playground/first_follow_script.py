from enum import Enum
import csv


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
                [Terminals.MINUS, Variables.EXPRARITH]
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

first_sets = {}
follow_sets = {}

"""This function computes the first set of a given variable in the grammar.
"""
def first(variable):
    if first_sets.get(variable) is not None:  # Base case: first set is already computed
        return first_sets[variable]

    first_sets[variable] = set()

    if variable in Terminals:  # Base case: variable is a terminal
        first_sets[variable].add(variable)
        return first_sets[variable]

    for production in grammar[variable]:
        if production[0] in Terminals:
            first_sets[variable].add(production[0])
        else:
            first_sets[variable] = first_sets[variable].union(first(production[0]))
            if Terminals.EPSILON in first(production[0]):
                first_sets[variable] = first_sets[variable].union(first(production[1]))

    return first_sets[variable]


"""This function computes the follow set of a given variable in the grammar.
It avoids infinite recursion by keeping track of the variables for which the follow set is already computed.
"""
def follow(variable):
    if follow_sets.get(variable) is not None:  # Base case: follow set is already computed
        return follow_sets[variable]

    follow_sets[variable] = set()

    if variable == Variables.PROGRAM:  # Base case: variable is the start symbol
        follow_sets[variable].add(Terminals.EOF)

    for key in grammar:
        for production in grammar[key]:
            if variable in production:
                if production.index(variable) == len(production) - 1:  # Case 1: variable is at the end of the production
                    if key != variable:
                        follow_sets[variable] = follow_sets[variable].union(follow(key))
                else:  # Case 2: variable is not at the end of the production
                    if production[production.index(variable) + 1] in Terminals:
                        follow_sets[variable].add(production[production.index(variable) + 1])
                    else:
                        follow_sets[variable] = follow_sets[variable].union(first(production[production.index(variable) + 1]))
                        if Terminals.EPSILON in first(production[production.index(variable) + 1]):
                            follow_sets[variable] = follow_sets[variable].union(follow(key))

    return follow_sets[variable]


def action_table():
    """ 
    This function computes the action table of this grammar. 
    It gives the absolute number of the rule generating the terminal in the cell corresponding to the variable and the terminal.
    Absolute number means the number of the rule in the grammar, not the number of the rule in the production.
    """
    action = {}
    rule_number = 0
def action_table():
    """ 
    This function computes the action table of this grammar. 
    It gives the absolute number of the rule generating the terminal in the cell corresponding to the variable and the terminal.
    Absolute number means the number of the rule in the grammar, not the number of the rule in the production.
    """
    action = {}
    rule_number = 0

    for variable in Variables:
        action[variable] = {}
        for terminal in Terminals:
            action[variable][terminal] = None

    for variable in grammar:
        for production in grammar[variable]:
            rule_number += 1
            for terminal in first_sets[variable]:
                if terminal != Terminals.EPSILON:
                    action[variable][terminal] = rule_number
            if Terminals.EPSILON in first_sets[variable]:
                for terminal in follow_sets[variable]:
                    action[variable][terminal] = rule_number
            if Variables.PROGRAM in first_sets[variable]:
                action[variable][Terminals.EOF] = rule_number

    return action
    
    
def action_table_to_csv():
    """ 
    This function writes the action table to a csv file. 
    """
    action = action_table()
    with open(r"C:\Users\brenn\IdeaProjects\infoh403_project\playground\action.csv", "w+", newline='') as f:
        writer = csv.writer(f, lineterminator='\n')
        writer.writerow([""] + [terminal.name for terminal in Terminals])
        for key in action:
            writer.writerow([key.name] + [action[key][terminal] for terminal in Terminals])

def generate_csv():
    with open(r"C:\Users\brenn\IdeaProjects\infoh403_project\playground\first.csv", "w+", newline='') as f:
        writer = csv.writer(f, lineterminator='\n')
        writer.writerow(["Variable", "FIRST"])
        for key in grammar:
            first_set = first(key)
            writer.writerow([f"<{key.name}>",f"{[item.value[0] if isinstance(item, Enum) else item for item in first_set]}"])

    with open(r"C:\Users\brenn\IdeaProjects\infoh403_project\playground\follow.csv", "w+", newline='') as f:
        writer = csv.writer(f, lineterminator='\n')
        writer.writerow(["Variable", "FOLLOW"])
        for key in grammar:
            follow_set = follow(key)
            writer.writerow([f"<{key.name}>",f"{[item.value[0] if isinstance(item, Enum) else item for item in follow_set]}"])

def main():
    with open(r"C:\Users\brenn\IdeaProjects\infoh403_project\playground\first.txt", "w+") as f:
        f.write("First sets:\n")
        first_sets = {key.name: first(key) for key in grammar}
        for key, value in first_sets.items():
            f.write(f"<{key}> : {[item.value[0] if isinstance(item, Enum) else item for item in value]}\n")

    with open(r"C:\Users\brenn\IdeaProjects\infoh403_project\playground\follow.txt", "w+") as f:
        f.write("\nFollow sets:\n")
        follow_sets = {key.name: follow(key) for key in grammar}
        for key, value in follow_sets.items():
            f.write(f"<{key}> : {[item.value[0] if isinstance(item, Enum) else item for item in value]}\n")

    action_table_to_csv()
    generate_csv()

        

if __name__ == "__main__":
    main()
        
        

if __name__ == "__main__":
    main()