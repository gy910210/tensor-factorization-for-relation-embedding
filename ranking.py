#!/usr/bin/python
# This script computes ranking for test.idx
# Input:    /dat/fb15k-intermediate/test.idx
#           /result/...
# Output:   /evaluation/rank15k_i[ITERATION]_d[DIM].txt

import numpy as np
from collections import OrderedDict
import sys
entityNum = 14505
relNum = 237
validNum = 17526
testNum = 20438

def logistic( e1, r1, e2 ):
    if e1.shape != (1, DIM) or e2.shape != (1, DIM) or r1.shape != (DIM, DIM):
        print "Dimension not matched!"
        return -1
    else:
        product = float ( e1 * r1 * e2.transpose() )
        return 1 / ( 1 + np.exp(-1 * product) )


def main():
    # Read valid file
    global DIM
    DIM = int(raw_input('enter dimension:'))
    ITERATION = raw_input('enter iteration time:')
    validFile = open('dat/fb15k-intermediate/test.idx', 'r')
    validIdx = []
    for line in validFile.readlines():
        splitted = line.split()
        e1 = int(splitted[0])
        e2 = int(splitted[2])
        r = int(splitted[1])
        validIdx = validIdx + [[e1,r,e2]]
    validFile.close()

    # Read entityMatrix 
    print 'Reading entity vectors and relation matrices'
    matFile = open('result/resultfb15k_i' + ITERATION + '_d' + str(DIM) + '.txt','r')
    matFile.readline()
    entityMat = np.ones((entityNum, DIM), dtype = float)
    relationTensor = np.ones((relNum, DIM, DIM), dtype = float)
    for i in range(entityNum):
        line = matFile.readline()
        splitted = line.split()
        entityMat[ i ] = map(float, splitted)

    matFile.readline()
    for i in range(relNum):
        tmp_relation_mat = np.ones((DIM, DIM), dtype = float )
        for k in range(DIM):
            line = matFile.readline()
            tmp_relation_mat[ k ] = map(float, line.split())
        relationTensor[ i ] = tmp_relation_mat.copy()
        matFile.readline()
    matFile.close()
    entityMat = np.asmatrix(entityMat)
    print 'Reading finished.'

    # Iterate over valid file
    print 'Ranking...'
    rankFile = open('evaluation/rank15k_i' + ITERATION+'_d' + str(DIM) + '.txt','w')
    for i in range(len(validIdx)):
        if i % 50 == 0:
            perc = int( 100.0 * i / len(validIdx) )
            sys.stdout.write('\r[%-50s] %d%% - %d' % ("=" *int(perc/2) , perc, i  ))
            sys.stdout.flush()

        e1 = validIdx[ i ][ 0 ]
        r = validIdx[ i ][ 1 ]
        logVal = {}
        for j in range(entityNum):
           logVal[ j ] = logistic( entityMat[ e1 ], relationTensor[ r ], entityMat[ j ] )  
        idxList = OrderedDict(sorted( logVal.items(), key = lambda t: t[1]))
        cnter = 1
        for idces in idxList:
            if validIdx[ i ][ 2 ] == idces:
                rankFile.write( str( cnter ) + '\n')
            cnter = cnter + 1
    rankFile.close()
    print ''

if __name__ == '__main__':
    main()

