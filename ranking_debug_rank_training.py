#!/usr/bin/python
# This script computes ranking for train500.idx
# Input:    /dat/fb15k-intermediate/train500.idx
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

def computeBatch(argu):
    trainFile = open('dat/fb15k-intermediate/train_divided/train500_d100/train500_' + str(argu) + '.idx', 'r')
    lineNum = 0
    for line in trainFile:
        lineNum += 1
        e1 = int( line.split()[0] )
        r = int( line.split()[1] )
        e2 = int( line.split()[2] )
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
                outFile.write(str(cnter) + '\t' + str(logVal[ e2 ]) + '\n')
                break
            cnter += 1
	for i in fin:
		i = 0
    trainFile.close()
    Fin += 1


def main():
    global entityMat, relationTensor
    readMat(dim = 5, iteration = 350)
    tic = time.clocl()
    global Fin,fin 
    Fin = 0
    fin = np.zeros([100,entityNum ], dtype = int)
    for i in range(100):
        threading.Thread( target = computeBatch, args = (j,) ).start()

    while Fin < 99:
        sys.stdout.write('\r[%-50s] %d%%' % ('=' * Fin / 2, Fin))
        sys.stdout.flush()
    print ''
    # ranking according to logistic value
    toc = time.clock()
    print 'time elapsed:',toc-tic
    outFile.close()



def readMat(dim, iteration):
    # Read test file
    # Read entityMatrix 
    matFile = open('result/resultfb15k_i' + str(iteration) + '_d' + str(dim) + '.txt','r')
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



if __name__ == '__main__':
    main()

