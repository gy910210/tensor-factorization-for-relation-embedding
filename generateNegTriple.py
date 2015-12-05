#!/usr/bin/python

# This script create negative(false) triples based on Freebase fb15k dataset
# Input: /dat/fb15k-intermediate/triple.idx (positive(true) triples in fb15k)
# Output: /dat/fb15k-intermediate/triple_negative.idx (negative(false) triples generated)
# Method: For each triple in triple.idx, first generate a random number within {1,2,3}, to decide
#         the pattern of the false example. 
#         Pattern 1: fix e1,r. Rand e2. Pattern 2: fix e1,e2. Rand r. Pattern 3: fix r,e2. Rand e1 
# Note:   The resulting triple_negative.idx has k times the size of triple.idx

import random
import sys

# Choose dataset, fb15k or fb30 for debugging
switch = raw_input('enter 30 for fb30, 15k for fb15k:')
if switch == '30':
    fileName = 'fb30'
    ENTITY_SIZE = 58
    RELATION_SIZE = 24
    TRIPLE_SIZE = 30
elif switch == '15k':
    fileName = 'fb15k'
    ENTITY_SIZE = 14505
    RELATION_SIZE = 237
    TRIPLE_SIZE = 272115
else:
    print "no dataset found"
    exit(0)

k = int( raw_input('enter k, k will be the ratio of neg dataset vs pos dataset:' ) )
random.seed(1)

# First fill the Positive set so that we know if a random generated 
# triple already exists
print 'loading positive dataset...'
posSet = set()
fread = open("dat/" + fileName + "-intermediate/triple.idx", "r")
cnter = 0
for line in fread.readlines():
    lineSplit = line.split()
    posSet.add( eval('(' + lineSplit[0] + ',' + lineSplit[1] + ',' + lineSplit[2] + ')' ))
    cnter += 1
    if cnter % 1000 == 0:
        perc = int( 100.0 * cnter / TRIPLE_SIZE)
        sys.stdout.write('\r[%-50s] %d%%'  % ('=' * int(perc/2) + '>', perc ))
        sys.stdout.flush()
sys.stdout.write('\r[%-50s] %d%%' % ('=' * 50, 100))
sys.stdout.flush()
print ''
fread.close()

# Generate negative examples
print 'start generating negative examples'
fread = open("dat/" + fileName + "-intermediate/triple.idx", "r")
fneg = open( "dat/" + fileName + "-intermediate/triple_negative.idx", 'w' )
generateCnter = 0
negSet = set()
total = len(posSet) * k
cnter = 0
for line in fread.readlines():
    lineSplit = line.split()
    trilogy = random.randint( 1, 3 )
    e1 = lineSplit[0]
    r = lineSplit[1]
    e2 = lineSplit[2]
    if trilogy == 1: # fix e1, r. Rand e2
       while generateCnter < k:
           tmp = random.randint( 0,ENTITY_SIZE - 1  )
           tmp_tuple = eval('(' + e1 + ',' + r + ',' + str( tmp ) + ')' )
           if tmp_tuple not in  posSet and tmp_tuple not in negSet:
                fneg.write(e1 + '\t' + r + '\t' + str(tmp) + '\n' )
                generateCnter = generateCnter + 1
                negSet.add(tmp_tuple)
    elif trilogy == 2:  # fix e1,e2. Rand r
         while generateCnter < k:
           tmp = random.randint( 0, RELATION_SIZE - 1)
           tmp_tuple = eval('(' + e1 + ',' + str(tmp) + ',' + e2 + ')' )
           if tmp_tuple not in  posSet and tmp_tuple not in negSet:
               fneg.write(e1 + '\t' + str(tmp) + '\t' + e2 + '\n')
               negSet.add(tmp_tuple)
               generateCnter = generateCnter + 1
    elif trilogy == 3: # fix e2,r. Rand e1
         while generateCnter < k:
           tmp = random.randint( 0, ENTITY_SIZE - 1)
           tmp_tuple = eval('(' + str(tmp) + ',' + r + ',' + e2 + ')' )
           if tmp_tuple not in posSet and tmp_tuple not in negSet:
                fneg.write( str(tmp)+ '\t' + r + '\t' + e2  + '\n' )
                generateCnter = generateCnter + 1
                negSet.add(tmp_tuple)
    generateCnter = 0
    cnter += 1
    if cnter % 1000 == 0:
        perc = int(100.0 * cnter / total)
        sys.stdout.write('\r[%-50s] %d%%' % ('=' * int( perc/2 ), perc ))
        sys.stdout.flush()
sys.stdout.write('\r[%-50s] %d%%' % ('=' * 50, 100))
sys.stdout.flush()
print ''
fread.close()
fneg.close()
