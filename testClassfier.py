#!/usr/bin/python
import numpy as np

switch = raw_input('enter 30 for fb30, 15k for fb15k:')
TARGET = raw_input('enter t to test Training, e to test Test, v to test Valid:')
DIM = int(raw_input('enter dimension:'))
ITERATION_TIME = int(raw_input('enter iteration time:')) 
DISP = raw_input('enter d to display detailed results, f to skip:')
if switch == '30':
    entityNum = 58
    relNum = 24
    fileName = 'fb30'
elif switch == '15k':
    entityNum = 14505
    relNum = 237
    fileName = 'fb15k'
else:
    print "no dataset found"
    exit(0)

def logistic( e1, r1, e2 ):
    if e1.shape != (1, DIM) or e2.shape != (1, DIM) or r1.shape != (DIM, DIM):
        print "Dimension not matched!"
        return -1
    else:
        product = float ( e1 * r1 * e2.transpose() )
        return 1 / ( 1 + np.exp(-1 * product) )


def main():
    resultFid = open('result/result' + fileName + '_i' + str( ITERATION_TIME ) + '_d' + str( DIM ) + '.txt','r')
    # Load result, aka entity vector and relation matrices
    resultFid.readline()
    entityMat = np.ones((entityNum, DIM), dtype=float)  # dat start from 0
    relationTensor = np.ones((relNum, DIM, DIM), dtype = float) # dat start from 0
    for i in range(entityNum):
        line = resultFid.readline()
        splitted = line.split()
        entityMat[ i ] = map(float, splitted)
    resultFid.readline()
    for j in range(relNum):
        tmp_relation_mat = np.ones((DIM,DIM), dtype = float )
        for k in range(DIM):
            line =  resultFid.readline()
            tmp_relation_mat[ k ] = map(float, line.split())
        relationTensor[j] = tmp_relation_mat.copy()
        resultFid.readline()
    resultFid.close()
    entityMat = np.asmatrix(entityMat)
    truePos = 0
    falsePos = 0
    falseNeg = 0
    
    if TARGET == 'e':
        tripleFid = open('dat/' + fileName + '-intermediate/test.idx','r')
    elif TARGET == 't':
        tripleFid = open('dat/' + fileName + '-intermediate/triple.idx','r')
        tripleNegFid = open('dat/' + fileName + '-intermediate/triple_negative.idx','r')
    else:
        tripleFid = open('dat/' + fileName + '-intermediate/valid.idx','r')

    for line in tripleFid.readlines():
        splitted = line.split()
        val = logistic( entityMat[ int( splitted[0] ) ], relationTensor[ int( splitted[1] )  ], entityMat[ int( splitted[2] ) ] ) 
        if DISP == 'd':
            print str(splitted) + '\t' + str(val)
        if val > 0.5 :
            truePos += 1
        elif val < 0.5:
            falseNeg += 1
    if TARGET == 't':
        for line in tripleNegFid.readlines():
            splitted = line.split()
            val = logistic( entityMat[ int( splitted[0] )  ], relationTensor[ int( splitted[1] ) ], entityMat[ int( splitted[2] ) ] ) 
            if DISP == 'd':
                print str(splitted) + '\t' + str(val)
            if val > 0.5:
                falsePos +=  1

    print 'truePos' + str(truePos)
    print 'falsePos' + str(falsePos)
    print 'falseNeg' + str(falseNeg)
    precision = float(truePos)/(truePos + falsePos)
    recall = float(truePos)/(truePos + falseNeg)
    fscore = 2 * precision * recall / (precision + recall)
    print 'precision:' + str(precision)
    print 'recall:' + str(recall)
    print 'fscore:' + str(fscore)
    tripleFid.close()
    tripleNegFid.close()

if __name__ == '__main__':
    main()
