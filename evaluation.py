import numpy as np
from collections import OrderedDict
entityNum = 14505
relNum = 237
validNum = 17535
testNum = 20466
DIM = 5

def logistic( e1, r1, e2 ):
    if e1.shape != (1, DIM) or e2.shape != (1, DIM) or r1.shape != (DIM, DIM):
        print "Dimension not matched!"
        return -1
    else:
        product = float ( e1 * r1 * e2.transpose() )
        return 1 / ( 1 + np.exp(product) )


def main():
    # Read valid file
    validFile = open('dat/fb15k-intermediate/test.idx', 'r')
    validIdx = []
    for line in validFile.readlines():
        splitted = line.split()
        if 'm' in splitted[0] or 'm' in splitted[2]:
            continue
        e1 = int(splitted[0]) 
        e2 = int(splitted[2])
        r = int(splitted[1])
        validIdx = validIdx + [[e1,r,e2]]
    validFile.close()

    # Read entityMatrix 
    matFile = open('result/resultfb15k_i15_d5.txt','r')
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


    # Iterate over valid file
    rankFile = open('evaluation/rank15k_i15_d5_test.txt','w')
    for i in range(len(validIdx)):
        print 'evaluating ' + str(i) + 'th triple'
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


if __name__ == '__main__':
    main()

