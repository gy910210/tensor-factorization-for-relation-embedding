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

def thd1():

    global thd1Fin, resultList, entityMat, relationTensor

    fid = open('dat/fb15k-intermediate/test_divided/test_d4/test_1.idx', 'r')
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
                 resultList[ i ] = cnter
            cnter = cnter + 1
    print 'Thread 1 finished.'
    thd1Fin = True



def thd2():

    global thd2Fin, resultList, entityMat, relationTensor

    fid = open('dat/fb15k-intermediate/test_divided/test_d4/test_2.idx', 'r')
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
                 resultList[ i + 5109 ] = cnter
            cnter = cnter + 1
    print 'Thread 2 finished.'
    thd2Fin = True




def thd3():

    global thd3Fin, resultList,  entityMat, relationTensor

    fid = open('dat/fb15k-intermediate/test_divided/test_d4/test_3.idx', 'r')
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
                 resultList[ i + 10218] = cnter
            cnter = cnter + 1
    print 'Thread 3 finished.'
    thd3Fin = True


def thd4():

    global  thd4Fin, resultList,  entityMat, relationTensor

    fid = open('dat/fb15k-intermediate/test_divided/test_d4/test_4.idx', 'r')
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
                 resultList[ i + 15327 ] = cnter
            cnter = cnter + 1
    print 'Thread 4 finished.'
    thd4Fin = True




def main():
    # Read valid file
    global DIM, resultList, thd1Fin, thd2Fin, thd3Fin, thd4Fin, entityMat, relationTensor
    DIM = int(raw_input('enter dimension:'))
    ITERATION = raw_input('enter iteration time:')
    thd1Fin, thd2Fin, thd3Fin, thd4Fin = False,  False, False, False


    tic = time.clock()
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

    Thd1 = threading.Thread( target = thd1 )
    Thd1.start()

    Thd2 = threading.Thread( target = thd2 )
    Thd2.start()

    Thd3 = threading.Thread( target = thd3 )
    Thd3.start()

    Thd4 = threading.Thread( target = thd4 )
    Thd4.start()

    while not (thd1Fin and thd2Fin and thd3Fin and thd4Fin ):
        pass

    rankFile = open('evaluation/rank15k_i' + ITERATION+'_d' + str(DIM) + '.txt','w')
    for i in range(len(resultList)):
        if i % 50 == 0:
            perc = int( 100.0 * i / testNum )
            sys.stdout.write('\r[%-50s] %d%% - %d' % ("=" *int(perc/2) , perc, i  ))
            sys.stdout.flush()
        rankFile.write( resultList[ i ] )
        rankFile.write( '\n' )
    toc = time.clock()
    print ''
    print 'Ranking finished, time elapsed: ', toc - tic


if __name__ == '__main__':
    main()

