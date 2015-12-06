#!/usr/bin/python
# This script computes ranking for test.idx, use multithreading to 
# improve performance
# Input:    /dat/fb15k-intermediate/test_divided/test_[1,2,3,4].idx
#           /result/...
# Output:   /evaluation/rank15k_i[ITERATION]_d[DIM].txt

import numpy as np
from collections import OrderedDict
import sys
import threading
import time
entityNum = 14505
relNum = 237
validNum = 17526
testNum = 20438

def logistic( e1, r1, e2 ):
    global DIM
    if e1.shape != (1, DIM) or e2.shape != (1, DIM) or r1.shape != (DIM, DIM):
        print "Dimension not matched!"
        return -1
    else:
        product = float ( e1 * r1 * e2.transpose() )
        return 1 / ( 1 + np.exp(-1 * product) )

def thd(arg):

    global FinList, resultList, entityMat, relationTensor
    offset = arg * 2043
    fid = open('dat/fb15k-intermediate/test_divided/test_d10/test_' + str(arg + 1) + '.idx', 'r')
    validIdx = []
    for line in fid.readlines():
        splitted = line.split()
        e1 = int(splitted[0])
        e2 = int(splitted[2])
        r = int(splitted[1])
        validIdx = validIdx + [[e1,r,e2]]
    fid.close()
    for i in range(len(validIdx)):
        e1 = validIdx[ i ][ 0 ]
        r = validIdx[ i ][ 1 ]
        logVal = {}
        for j in range(entityNum):
           logVal[ j ] = logistic( entityMat[ e1 ], relationTensor[ r ], entityMat[ j ] )  
        idxList = OrderedDict(sorted( logVal.items(), key = lambda t: t[1]))
        cnter = 1
        for idces in idxList:
            if validIdx[ i ][ 2 ] == idces:
                 resultList[ i + offset ] = cnter
            cnter = cnter + 1

    FinList[ arg ] = 1

def main():
    # Read valid file
    global DIM, resultList, FinList, entityMat, relationTensor
    DIM = int(raw_input('enter dimension:'))
    ITERATION = raw_input('enter iteration time:')

    tic = time.clock()
    thdFinList = np.zeros([10], dtype = int )
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

    resultList = np.zeros([testNum], dtype = int)
    # Iterate over valid file
    print 'Ranking...'

    for i in range(10):
        thdi = threading.Thread( target = thd, args = (i,) )
        thdi.start()

    while not thdFinList.all():
        pass

    rankFile = open('evaluation/rank15k_i' + ITERATION+'_d' + str(DIM) + '_2.txt','w')
    for i in range(len(resultList)):
        rankFile.write( resultList[ i ] )
        rankFile.write( '\n' )
    toc = time.clock()
    print 'Ranking finished, time elapsed: ', toc - tic


if __name__ == '__main__':
    main()

