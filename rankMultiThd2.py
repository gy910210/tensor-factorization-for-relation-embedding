#!/usr/bin/python
# This script computes ranking for triple.idx, use multithreading to 
# improve performance
# Input:    /dat/fb15k-intermediate/train_divided/train_[0 - 99].idx
#           /result/...
# Output:   /ranking/rank15k_i[ITERATION]_d[DIM].txt

import numpy as np
from collections import OrderedDict
import sys
import threading
import time
ENTITY_NUM = 14505
RELATION_NUM = 237
DIM = 5
ITERATION = 350
TRIPLE_NUM = 2370

def main():
    global entityMat, relationTensor, reinforceList
    tic = time.clock()
    reinforceList = []
    readTrainedResults()
    print 'Ranking...'
    fid = raw_input('enter fid(0 - 99):')
    ranker(fid)
    rankFile = open('ranking/r_i' + str(ITERATION)+'_d' + str(DIM) + '_nr_100/'+fid+'.txt','w')
    for i in reinforceList:
        for j in i:
            rankFile.write( str(j) + '\t' )
        rankFile.write( '\n' )
    toc = time.clock()
    print 'Ranking finished, time elapsed: ', toc - tic



def ranker( fid ):
    global  entityMat, relationTensor, validIdx, reinforceList
    fin = open('dat/fb15k-intermediate/train_divided/train_' + fid  + '.idx', 'r')
    validIdx = []
    for line in fin.readlines():
        splitted = line.split()
        e1 = int(splitted[0])
        e2 = int(splitted[2])
        r = int(splitted[1])
        validIdx = validIdx + [[e1,r,e2]]
        reinforceList += [[]]
    fin.close()
    threads = []
    for i in range(len(validIdx)):
        t = threading.Thread( target = kobito, args = (i,))
        t.start()
        threads.append(t)
    for t in threads:
        t.join()

    return


def kobito( idx ):
    print 'ranking ',idx
    global entityMat, relationTensor, validIdx
    e1Id = validIdx[idx][0]
    rId = validIdx[idx][1]
    e2Id = validIdx[idx][2]
    pre = entityMat[ e1Id ] * relationTensor[ rId ]
    validValue = pre * entityMat[ e2Id ].transpose()
    # threads = []
    for i in range(ENTITY_NUM):
        if( pre * entityMat[ i ].transpose() ) > validValue:
            reinforceList[idx] += [ i ]
    
    #    t = threading.Thread( target = computeLogThd, args = (idx, pre, i, validValue,) )
    #    t.start()
    #    threads.append(t)
    '''
    for t in threads:
        t.join()
    '''
    print 'ranked ',idx
    return

def computeLogThd(idx, pre, e2, validValue):
    global entityMat, reinforceList
    if ( pre * entityMat[ e2 ].transpose() ) > validValue:
        reinforceList[idx] += [ e2 ]
    return

def readTrainedResults():
    global entityMat, relationTensor
    fin = open('result/trained_i' + str(ITERATION) + '_d' + str(DIM) + '.txt', 'r')
    fin.readline()
    entityMat = np.ones( (ENTITY_NUM, DIM), dtype = float)
    relationTensor = np.ones( ( RELATION_NUM, DIM, DIM ), dtype = float)
    for i in range(ENTITY_NUM):
        line = fin.readline()
        splitted = line.split()
        entityMat[ i ] = map(float, splitted)
    fin.readline()
    for j in range(RELATION_NUM):
        tmp_relation_mat = np.ones((DIM, DIM), dtype = float)
        for k in range(DIM):
            line = fin.readline()
            tmp_relation_mat[ k ] = map(float, line.split())
        relationTensor[ j ] = tmp_relation_mat.copy()
        fin.readline()
    fin.close()
    entityMat = np.asmatrix( entityMat )


def logistic( e1, r1, e2 ):
    product = float ( e1 * r1 * e2.transpose() )
    return 1 / ( 1 + np.exp(-1 * product) )



if __name__ == '__main__':
    main()

