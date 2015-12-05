#!/usr/bin/python
# This scripts generate index for entity and relations in train.txt
# Input:    /dat/fb15k/train.txt
# Output:   /dat/fb15k-intermediate/entity.idx
#           /dat/fb15k-intermediate/relation.idx
#           /dat/fb15k-intermediate/triple.idx
# Method:   read train.txt, idx the entities and relations
#           then read train.txt again, generate triple.idx 
#           using the indeces generated

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

entityList = []
relationList = []
fread = open("dat/" + fileName + "/train.txt", "r")
fwriteE = open("dat/" + fileName + "-intermediate/entity.idx", 'w')
fwriteR = open("dat/" + fileName + "-intermediate/relation.idx", 'w')
for line in fread.readlines():
    lineSplit = line.split()
    e1 = lineSplit[0]
    e2 = lineSplit[2]
    r = lineSplit[1]
    if not e1 in entityList:
        entityList += [e1]
    if not e2 in entityList:
        entityList += [e2]
    if not r in relationList:
        relationList += [r]

fread.close()

for i in range(len(entityList)):
    fwriteE.write(str(i) + '\t' + entityList[i] + '\n')

for i in range(len(relationList)):
    fwriteR.write(str(i) + '\t' + relationList[i] + '\n')

fwriteE.close()
fwriteR.close()

fread2 = open("dat/" + fileName + "/train.txt", "r")
fwriteT = open("dat/" + fileName + "-intermediate/triple.idx", 'w')

for line in fread2.readlines():
    lineSplit = line.split()
    e1Idx = entityList.index(lineSplit[0])
    rIdx = relationList.index(lineSplit[1])
    e2Idx = entityList.index(lineSplit[2])
    fwriteT.write(str(e1Idx) + '\t' + str(rIdx) + '\t' + str(e2Idx)+ '\n');

fread2.close()
fwriteT.close()
