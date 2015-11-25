#!/usr/bin/python

# This script generates Include file to facilitate training program
# Input:/dat/fb15k-intermediate/triple.idx
#       /dat/fb15k-intermediate/triple_neg_random.idx
# Output:   /dat/fb15k-intermediate/inc_left.list
#           /dat/fb15k-intermediate/inc_right.list
#           /dat/fb15k-intermediate/inc_rel.list
# Method:   inc_left.list consists of multiple rows to store the list of 
#           triple indeces. The row number represents the entity index
#           and the list following the row number is the list of triple
#           indeces such that among all those triples the entity[row] 
#           exists as the left part(subj)
#           Likewise for the inc_right.list and inc_rel.list
# Note:     All indeces starts from 0 to size-1


import numpy as np

# Choose dataset, fb30 is for debugging
switch = raw_input('enter 30 for fb30, 15k for fb15k, q for quit:')
if switch == '30':
    ENTITY_SIZE = 58
    RELATION_SIZE = 24
    fileName = 'fb30'
elif switch == '15k':
    ENTITY_SIZE = 14505
    RELATION_SIZE = 237
    fileName = 'fb15k'
elif switch == 'q':
    print 'exit!'
    exit(0)
else:
    exit(0)


inc_leftFid = open( 'dat/' + fileName + '-intermediate/inc_left.list' , 'w' )
inc_rightFid = open( 'dat/' + fileName + '-intermediate/inc_right.list', 'w' )
inc_relFid = open( 'dat/' + fileName + '-intermediate/inc_rel.list', 'w' )
triple_posFid = open( 'dat/' + fileName + '-intermediate/triple.idx', 'r' )
triple_negFid = open( 'dat/' + fileName + '-intermediate/triple_neg_random.idx', 'r')

# Prepare lists to store results
# inc_leftList, inc_rightList and inc_relList are all list of lists
# Initialize them all with list of empty lists
inc_leftList, inc_rightList, inc_relList = [],[],[]
for i in range( ENTITY_SIZE ):
    inc_leftList += [[]]
    inc_rightList += [[]]

for i in range( RELATION_SIZE ):
    inc_relList += [[]]

# Start filling the results
# cnt is used to hold for row number(aka. triple index)
cnt = 0
for line in triple_posFid.readlines():
    splitted = line.split()
    leftIdx =  int( splitted[ 0 ] ) 
    relIdx =  int( splitted[ 1 ] ) 
    rightIdx =  int( splitted[ 2 ] ) 
    inc_relList[ relIdx ] += [ cnt ]
    inc_leftList[ leftIdx ] += [ cnt ]
    inc_rightList[ rightIdx ] += [ cnt ]
    cnt += 1

triple_posFid.close()

subcnt = 0
for line in triple_negFid.readlines():
    splitted = line.split()
    leftIdx =  int( splitted[ 0 ] ) 
    relIdx =  int( splitted[ 1 ] ) 
    rightIdx =  int( splitted[ 2 ] ) 
    inc_relList[ relIdx ] += [ cnt ]
    inc_leftList[ leftIdx ] += [ cnt ]
    inc_rightList[ rightIdx ] += [ cnt ]
    cnt += 1
    subcnt+=1

triple_negFid.close()


# Write entity results to file, skip those entities that have empty list
for i in range( ENTITY_SIZE ):
    if len( inc_leftList[ i ] ) > 0:
        inc_leftFid.write( str(i) + ':' )
        for j in inc_leftList[ i ]:
            inc_leftFid.write( str(j) + '\t' )
        inc_leftFid.write('\n')

    if len( inc_rightList[ i ] ) > 0:
        inc_rightFid.write( str(i) + ':' )
        for j in inc_rightList[ i ]:
            inc_rightFid.write( str(j) + '\t' )
        inc_rightFid.write('\n')

inc_leftFid.close()
inc_rightFid.close()

# Write relation results to file, skip those relations that have empty list
for i in range( RELATION_SIZE ):
    if len( inc_relList[ i ] ) > 0:
        inc_relFid.write( str(i) + ':' )
        for j in inc_relList[ i ]:
            inc_relFid.write( str(j) + '\t' )
        inc_relFid.write('\n')

inc_relFid.close()
