#!/usr/bin/python

# This script sample some negative(false) triples generated by createNegativeCase.py
# Input: /dat/fb15k-intermediate/triple_negative.idx (original negative(false) triples in fb15k)
# Output: /dat/fb15k-intermediate/triple_neg_random.idx (random negative(false) triples generated)
# Method: Randomly sample negative samples in original generated pool

import random
random.seed(1)
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



negFid = open('dat/' + fileName + '-intermediate/triple_negative.idx','r')
outFid = open('dat/' + fileName + '-intermediate/triple_neg_random.idx','w')

idx = []

cnter = 0
sub_cnt = 0
for line in negFid.readlines():
	cnter += 1
	if  cnter % 1000 == 0:
		print sub_cnt
	a = random.randint(0,4)
	if a == 2:
		idx += [ cnter ]
        	sub_cnt+=1
        	outFid.write(line)




negFid.close()
outFid.close()
