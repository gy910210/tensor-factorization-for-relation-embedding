import numpy as np
from collections import OrderedDict
entityNum = 14505
relNum = 237
DIM = 5

def logistic( e1, r1, e2 ):
    if e1.shape != (1, DIM) or e2.shape != (1, DIM) or r1.shape != (DIM, DIM):
        print "Dimension not matched!"
        return -1
    else:
        product = float ( e1 * r1 * e2.transpose() )
        return 1 / ( 1 + np.exp(product) )


def main():
    filename = raw_input('please enter filename( t for test.txt, v for valid.txt,q to quit ):')

    if filename == 't':
        fid = open('dat/fb15k-intermediate/test.idx','r')
    elif filename == 'v':
        fid = open('dat/fb15k-intermediate/valid.idx','r')
    elif filaname == 'q':
        print 'exit!'
        exit(0)
    else:
        print 'no dataset'
        exit(0)

    datList = []
    for line in fid.readlines():
        splitted = line.split()
        if 'm' in splitted[0] or 'm' in splitted[2]:
            datList = datList + [[-1,-1,-1]]
        else:
            datList = datList + [[ int(splitted[0]), int(splitted[1]), int(splitted[2])]]
    fid.close()

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


    while True:
        switch = raw_input('enter q to quit, r to check rank of groundtruth entity, s to show log value for given entity:')
        if switch == 'q':
            print 'quit'
            exit(0)

        row_number = int( raw_input('please enter row number') )
        # Iterate over valid file
        print 'evaluating ' + str(row_number) + 'th triple'
        e1 = datList[ row_number ][ 0 ]
        r = datList[ row_number ][ 1 ]
        print 'entity 1:' + str(e1) + '\t' + 'relation:' + str(r)
        logVal = {}
        for j in range(entityNum):
           logVal[ j ] = logistic( entityMat[ e1 ], relationTensor[ r ], entityMat[ j ] )  
        idxList = OrderedDict(sorted( logVal.items(), key = lambda t: t[1]))

        if switch == 'r':
            cnter = 1
            for idces in idxList:
                if datList[ row_number ][ 2 ] == idces:
                    break
                cnter = cnter + 1
            print 'value of ground truth event:' + str(idxList[ datList[row_number][2] ] )
            print 'Rank of ground truth event:' + str(cnter)
        elif switch == 's':
            eIdx = int(raw_input('enter entity idx, to show logistic value:'))
            print 'logistic value:' + str(logVal[ eIdx ] )

if __name__ == '__main__':
    main()

