#!/usr/bin/python
# This script computes ranking for test.idx
# Input:    /dat/fb15k-intermediate/test.idx
#           /result/...
# Output:   /evaluation/rank15k_i[ITERATION]_d[DIM].txt

import numpy as np
import time
from collections import OrderedDict
import sys
import threading
entityNum = 14505
relNum = 237
def logistic( e1, r1, e2 ):
    if e1.shape != (1, DIM) or e2.shape != (1, DIM) or r1.shape != (DIM, DIM):
        print "Dimension not matched!"
        return -1
    else:
        product = float ( e1 * r1 * e2.transpose() )
        return 1 / ( 1 + np.exp(-1 * product) )

def computeLogistic(argu, e1, r):
    global logVal, fin, entityMat, relationTensor
    logVal[ argu ] = logistic( entityMat[ e1 ], relationTensor[ r ], entityMat[ argu ] )
    fin += [1]

def main():
    # Read test file
    global DIM
    DIM = 5
    ITERATION = '350'
    # Read entityMatrix 
    matFile = open('result/resultfb15k_i' + ITERATION + '_d' + str(DIM) + '.txt','r')
    matFile.readline()
    global entityMat, relationTensor
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

    # ranking according to logistic value
    while True:
        switch = raw_input('q to quit, c to continue:')
        if switch == 'q':
            break
        else:
            e1 = int( raw_input( 'enter entity1:' ) )
            r = int( raw_input( 'enter relation: ' ) )
            e2 = int( raw_input( 'enter entity2:' ) )
            tic = time.clock()
            global logVal, fin
            logVal = {}
            fin = []
            for j in range(entityNum):
                    threading.Thread( target = computeLogistic, args = (j, e1, r,) ).start()
            while len(fin) < entityNum:
                pass
            print 'logistic value:\t',logVal[ e2 ]
            idxList = OrderedDict(sorted( logVal.items(), key = lambda t: t[1], reverse=True))
            cnter = 1
            for idces in idxList:
                if idces == e2:
                    print cnter
                    break
                cnter += 1
            toc = time.clock()
            print 'time elapsed:',toc-tic

if __name__ == '__main__':
    main()

