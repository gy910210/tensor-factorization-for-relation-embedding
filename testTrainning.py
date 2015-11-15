import numpy as np

switch = raw_input('enter 30 for fb30, 15k for fb15k:')
ITERATION_TIME = int(raw_input('enter iteration time:')) 
if switch == '30':
    entityNum = 58
    relNum = 24
    fileName = 'fb30'
    tripleNum = 30
elif switch == '15k':
    entityNum = 14505
    relNum = 237
    fileName = 'fb15k'
    tripleNum = 272115
else:
    print "no dataset found"
    exit(0)
DIM =5 

def logistic( e1, r1, e2 ):
    if e1.shape != (1, DIM) or e2.shape != (1, DIM) or r1.shape != (DIM, DIM):
        print "Dimension not matched!"
        return -1
    else:
        product = float ( e1 * r1 * e2.transpose() )
        return 1 / ( 1 + np.exp(product) )



def main():
    resultFid = open('result/result' + fileName + '_i' + str( ITERATION_TIME ) + '_d' + str( DIM ) + '.txt','r')
    tripleFid = open('dat/' + fileName + '-intermediate/triple.idx','r')

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

    for line in tripleFid.readlines():
        splitted = line.split()
        val = logistic( entityMat[ int( splitted[0] ) - 1 ], relationTensor[ int( splitted[1] ) - 1], entityMat[ int( splitted[2] ) - 1] ) 
        #print val
        gt = int(splitted[3])
        if val > 0.5 and gt == 1:
            truePos = truePos + 1
        elif val > 0.5 and gt == 0:
            falsePos = falsePos + 1
        elif val < 0.5 and gt == 1:
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
    tripleFid.close()


if __name__ == '__main__':
    main()
