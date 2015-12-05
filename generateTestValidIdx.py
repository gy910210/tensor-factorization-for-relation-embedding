#!/usr/bin/python
# This script generates index for test.txt and valid.txt, given entity.idx and relation.idx
# Input:    /dat/fb15k/test.txt
#           /dat/fb15k/valid.txt
#           /dat/fb15k-intermediate/entity.idx
#           /dat/fb15k-intermediate/relation.idx
# Output:   /dat/fb15k-intermediate/test.idx
#           /dat/fb15k-intermediate/valid.idx

freadE =  open("dat/fb15k-intermediate/entity.idx", "r")
freadR=  open("dat/fb15k-intermediate/relation.idx", "r")
freadT =  open("dat/fb15k/test.txt", "r")
freadV =  open("dat/fb15k/valid.txt", "r")

fwriteT = open("dat/fb15k-intermediate/test.idx", "w")
fwriteV = open("dat/fb15k-intermediate/valid.idx", "w")

entityList = []
relationList = []

for line in freadE.readlines():
    lineSplit = line.split()
    entityList += [lineSplit[1]]

for line in freadR.readlines():
    lineSplit = line.split()
    relationList += [lineSplit[1]]

freadE.close()
freadR.close()

for line in freadT.readlines():
    lineSplit = line.split()
    try:
        e1idx = entityList.index(lineSplit[0])
        e2idx = entityList.index(lineSplit[2])
        ridx = relationList.index(lineSplit[1])
    except ValueError,e:
        print e
        print lineSplit[0],lineSplit[1],lineSplit[2]
        continue
    else:
        fwriteT.write(str(e1idx) + '\t' + str(ridx) + '\t' + str(e2idx) + '\n')

for line in freadV.readlines():
    lineSplit = line.split()
    try:
        e1idx = entityList.index(lineSplit[0])
        e2idx = entityList.index(lineSplit[2])
        ridx = relationList.index(lineSplit[1])
    except ValueError,e:
        print e
        print lineSplit[0],lineSplit[1],lineSplit[2]
        continue
    else:
        fwriteV.write(str(e1idx) + '\t' + str(ridx) + '\t' + str(e2idx) + '\n')
fwriteV.close()
fwriteT.close()





