import nltk

stemmer = nltk.stem.porter.PorterStemmer()

OPERATORS_PRECEDENCE = {
    'NOT': 3,
    'AND': 2,
    'OR': 1
}

def get_terms(query):
    return query.strip().replace('(', '( ').replace(')', ' )').split(" ")


def reverse_polish(query):
    terms = get_terms(query)

    stack = []
    queue = []
    for term in terms:
        if term in OPERATORS_PRECEDENCE:
            if term != 'NOT':
                while len(stack) > 0 and stack[len(stack) - 1] and stack[len(stack) - 1] in OPERATORS_PRECEDENCE and OPERATORS_PRECEDENCE[stack[len(stack) - 1]] >= OPERATORS_PRECEDENCE[term]:
                    queue.append(stack.pop())

            stack.append(term)

        elif term == '(':
            stack.append(term)
        elif term == ')':
            while len(stack) > 0 and stack[len(stack) - 1] != '(':
                queue.append(stack.pop())

            if len(stack) > 0:
                stack.pop()
        else:
            processed_term = stemmer.stem(term.strip().lower())
            queue.append(processed_term)  

    queue.extend(reversed(stack))
    return queue



# print(reverse_polish("2 AND 3 OR 4 AND NOT 7"))
