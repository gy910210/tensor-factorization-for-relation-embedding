import numpy as np
entityNum = 14505
relNum = 237
DIM =10 

def logistic( e1, r1, e2 ):
    if e1.shape != (1, DIM) or e2.shape != (1, DIM) or r1.shape != (DIM, DIM):
        print "Dimension not matched!"
        return -1
    else:
        product = float ( e1 * r1 * e2.transpose() )
        return 1 / ( 1 + np.exp(product) )



def main():
    resultFid = open('result/resultfb15k_i15_d10.txt','r')
    validFid = open('dat/fb15k-intermediate/valid.idx','r')

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

    for line in validFid.readlines():
        splitted = line.split()
        if 'm' in splitted[0] or 'm' in splitted[2]:
            continue
        val = logistic( entityMat[ int( splitted[0] ) ], relationTensor[ int( splitted[1] )], entityMat[ int( splitted[2] ) ] ) 
        #print val
        if val > 0.5:
            truePos = truePos + 1
        elif val < 0.5:
            falseNeg = falseNeg + 1
    print 'truePos' + str(truePos)
    print 'falsePos' + str(falsePos)
    print 'falseNeg' + str(falseNeg)
    precision = float(truePos)/(truePos + falsePos)
    recall = float(truePos)/(truePos + falseNeg)
    fscore = 2 * precision * recall / (precision + recall)
    print 'precision:' + str(precision)
    print 'recall:' + str(recall)
    print 'fscore:' + str(fscore)
    validFid.close()


if __name__ == '__main__':
    main()
