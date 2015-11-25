#!/usr/bin/python

# This script create negative(false) triples based on Freebase fb15k dataset
# Input: /dat/fb15k-intermediate/triple.idx (positive(true) triples in fb15k)
# Output: /dat/fb15k-intermediate/triple_negative.idx (negative(false) triples generated)
# Method: For each triple in triple.idx, first generate a random number within {1,2,3}, to decide
#         the pattern of the false example. 
#         Pattern 1: fix e1,r. Rand e2. Pattern 2: fix e1,e2. Rand r. Pattern 3: fix r,e2. Rand e1 
# Note:   The resulting triple_negative.idx has five times the size of triple.idx

import random

# Choose dataset, fb15k or fb30 for debugging
switch = raw_input('enter 30 for fb30, 15k for fb15k:')
if switch == '30':
    fileName = 'fb30'
    ENTITY_SIZE = 58
    RELATION_SIZE = 24
elif switch == '15k':
    fileName = 'fb15k'
    ENTITY_SIZE = 14505
    RELATION_SIZE = 237
else:
    print "no dataset found"
    exit(0)

random.seed(1)

# First fill the Positive set so that we know if a random generated 
# triple already exists
posSet = set()
fread = open("dat/" + fileName + "-intermediate/triple.idx", "r")
for line in fread.readlines():
    lineSplit = line.split()
    posSet.add( eval('(' + lineSplit[0] + ',' + lineSplit[1] + ',' + lineSplit[2] + ')' ))
fread.close()

# Generate negative examples
fread = open("dat/" + fileName + "-intermediate/triple.idx", "r")
fneg = open( "dat/" + fileName + "-intermediate/triple_negative.idx", 'w' )
generateCnter = 0
negSet = set()
for line in fread.readlines():
    lineSplit = line.split()
    trilogy = random.randint( 1, 3 )
    e1 = lineSplit[0]
    r = lineSplit[1]
    e2 = lineSplit[2]
    if trilogy == 1: # fix e1, r. Rand e2
       while generateCnter < 5:
           tmp = random.randint( 0,ENTITY_SIZE - 1  )
           tmp_tuple = eval('(' + e1 + ',' + r + ',' + str( tmp ) + ')' )
           if tmp_tuple not in  posSet and tmp_tuple not in negSet:
                fneg.write(e1 + '\t' + r + '\t' + str(tmp) + '\n' )
                generateCnter = generateCnter + 1
                negSet.add(tmp_tuple)
    elif trilogy == 2:  # fix e1,e2. Rand r
         while generateCnter < 5:
           tmp = random.randint( 0, RELATION_SIZE - 1)
           tmp_tuple = eval('(' + e1 + ',' + str(tmp) + ',' + e2 + ')' )
           if tmp_tuple not in  posSet and tmp_tuple not in negSet:
               fneg.write(e1 + '\t' + str(tmp) + '\t' + e2 + '\n')
               negSet.add(tmp_tuple)
               generateCnter = generateCnter + 1
    elif trilogy == 3: # fix e2,r. Rand e1
         while generateCnter < 5:
           tmp = random.randint( 0, ENTITY_SIZE - 1)
           tmp_tuple = eval('(' + e1 + ',' + r + ',' + str(tmp) + ')' )
           if tmp_tuple not in posSet and tmp_tuple not in negSet:
                fneg.write( e1 + '\t' + r + '\t' + str(tmp)  + '\n' )
                generateCnter = generateCnter + 1
                negSet.add(tmp_tuple)
    generateCnter = 0

fread.close()
fneg.close()
