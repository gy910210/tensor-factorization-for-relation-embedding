#! /usr/bin/python
import numpy as np
import time
import math
import sys
np.random.seed(20)

ALPHA = 0.0005
BETA = 0.0005


# e1 and e2 are supposed to be 1 x DIM vectors
# r1 is supposed to be DIM x DIM matrix
# return sigmoid function value
def logistic( e1, r1, e2 ):
    if e1.shape != (1, DIM) or e2.shape != (1, DIM) or r1.shape != (DIM, DIM):
        print "Dimension not matched!"
        return -1
    else:
        product = float ( e1 * r1 * e2.transpose() )
        return 1 / ( 1 + np.exp(-1 * product) )


# Read triple idx
# return matrix of triple with annotation
def readTripleToMat( ):
    negFid =  open( 'dat/' + fileName + '-intermediate/triple_neg_random.idx', 'r')
    posFid =  open( 'dat/' + fileName + '-intermediate/triple.idx', 'r')
    mat = np.asmatrix([0,0,0,0])
    if fileName == 'fb30':
        total = 71
    else:
        total = 272115 + 272647
    cnter = 0

    for line in posFid.readlines():
        lineSplit = line.split()
        newrow = [ int( lineSplit[0] ), int( lineSplit[1] ), int( lineSplit[2] ),  1]
        mat = np.vstack([ mat, newrow ])
        cnter += 1
        if cnter % 5000 == 0:
            perc = int(100.0 * cnter / total)
            sys.stdout.write('\r[%-50s] %d%%' % ("=" *int(perc/2) , perc  ))
            sys.stdout.flush()


    for line in negFid.readlines():
        lineSplit = line.split()
        newrow = [ int( lineSplit[0] ), int( lineSplit[1] ), int( lineSplit[2] ),  0]
        mat = np.vstack([mat, newrow])
        cnter += 1
        if cnter % 5000 == 0:
            perc = int(100.0 * cnter / total)
            sys.stdout.write('\r[%-50s] %d%%' % ("=" *int(perc/2) , perc ))
            sys.stdout.flush()
    sys.stdout.write('\r[%50s] %d%%' % ("=" * 50, 100))
    sys.stdout.flush()
    print ''

    negFid.close()
    posFid.close()
    return mat


# Read inc_xx.list
# return dictionary, key is the idx and value is list of triples
def readIncToDic( fid ):
    d = dict()
    for lines in fid.readlines():
        split1 = lines.split(':')
        split2 = split1[1].split('\t')
        d[ int( split1[ 0 ] ) ] = map(int, split2[:-1] )
    return d

# Compute the derivative of loss function with respect to Entity vector i
def derivativeToE( i ):
    rst = np.zeros([1, DIM])
    tmp_this = entityMat[ i ]
    if leftDict.has_key( i ):
        for k in leftDict[ i ]: # k is triple index
            tmp_triple = tripleIdxMat[ k + 1 ] # since the first row is not used, the triple idx matrix starts from 1
            tmp_rel = relationTensor[ tmp_triple[ 0, 1 ]  ] # matrix for rm
            tmp_right = entityMat[ tmp_triple[ 0, 2 ]  ]  # matrix for ej
            part0 = tmp_triple[0, 3]
            part1 =  logistic( tmp_this, tmp_rel, tmp_right )
            part2 = tmp_right * tmp_rel.transpose()
            #part2 = ( tmp_rel.dot( tmp_right.transpose() ) ).transpose()
#            print 'LeftDict:\tpart0: ',part0,'\tpart1: ',part1,'\tpart2: ',part2
            rst +=  ( part0 - part1 ) * part2
    if rightDict.has_key( i ):
        for k in rightDict[ i ]:
            tmp_triple = tripleIdxMat[ k + 1 ]
            tmp_rel = relationTensor[ tmp_triple[ 0, 1 ] ] # matrix for rm
            tmp_left = entityMat[ tmp_triple[ 0, 0 ] ]  # matrix for ej
            part1 = logistic( tmp_left, tmp_rel, tmp_this )
            part2 = tmp_triple[0,3] - part1
#print 'RightDict:\tpart1: ',part1,'\tpart2: ',part2
            rst += part2 * ( tmp_left * tmp_rel )
    return -1 * rst

# Compute loss function
def computeLoss():
    rst = 0.0
    sampleSize = tripleIdxMat.shape[0]
    for i in range(1, sampleSize):
       currentRow = tripleIdxMat[ i ]
       y = currentRow[ 0,3 ]
       hx = logistic( entityMat[ currentRow[ 0, 0 ] ], relationTensor[ currentRow[ 0, 1 ] ], entityMat[ currentRow[ 0, 2 ]] )
       rst += y * math.log( hx ) + ( 1 - y ) * math.log( 1 - hx )
    return -1 * rst


# Compute the derivative of loss function with respect to relation matrix rm
def derivativeToR( block ):
    rst = np.zeros([DIM, DIM])
    tmp_this = relationTensor[ block ]
    for k in relDict[ block ]:
        tmp_triple = tripleIdxMat[ k + 1 ]
        tmp_left = entityMat[ tmp_triple[ 0, 0 ]  ] # vector for ei
        tmp_right = entityMat[ tmp_triple[ 0, 2 ]  ]  # vector for ej
        rst += ( tmp_triple[ 0, 3 ] - logistic( tmp_left, tmp_this, tmp_right ) ) * ( tmp_left.transpose() ).dot( tmp_right )

    return -1 * rst

def main():

    # -- Input arguments --
    switch = raw_input('enter 30 for fb30, 15k for fb15k, q for quit:')
    global fileName
    global ITERATION_TIME
    global DIM
    global computeLossFunction
    if switch == '30':
        ENTITY_SIZE = 58
        RELATION_SIZE = 24
        fileName = 'fb30'
    elif switch == '15k':
        ENTITY_SIZE = 14505
        RELATION_SIZE = 237
        fileName = 'fb15k'
    elif switch == 'q':
        print "exit!"
        exit(0)
    else:
        print "no dataset"
        exit(0)

    # -- Loading the triple matrix ( Positive and Negative ) --
    print 'start loading triple matrix(This is gonna take long) ...'
    tic = time.clock()
    global tripleIdxMatOrigin
    tripleIdxMatOrigin = readTripleToMat()
    toc = time.clock()
    print 'triple idx loaded. Time elapsed: ' + str( toc - tic )

    # -- Loading inc_left.list --
    print 'start loading inc_left.list...'
    fidLeft = open('dat/'+ fileName +'-intermediate/inc_left.list', 'r')
    global leftDict
    leftDict = readIncToDic( fidLeft )
    fidLeft.close()

    # -- Loading inc_right.list --
    print 'start loading inc_right.list...'
    fidRight = open('dat/' + fileName + '-intermediate/inc_right.list', 'r')
    global rightDict
    rightDict = readIncToDic( fidRight )
    fidRight.close()

    # -- Loading inc_rel.list --
    print 'start loading inc_rel.list...'
    fidRel = open('dat/' + fileName + '-intermediate/inc_rel.list', 'r')
    global relDict
    relDict = readIncToDic( fidRel )
    fidRel.close()

    while True:
        switch = raw_input('enter q to quit, c to continue:')
        if switch == 'q':
            print "exit!"
            exit(0)

        DIM = int(raw_input('enter vector dimension:'))
        ITERATION_TIME = int(raw_input('enter iteration time:'))
        computeLossFunction = raw_input('enter t to computer loss function, f to skip:')
        # Initialize matrix and tensor
        global entityMat
        global relationTensor
        entityMat = np.asmatrix( np.random.rand( ENTITY_SIZE, DIM ) )
        relationTensor =  np.random.rand( RELATION_SIZE, DIM, DIM )

        global tripleIdxMat
        tripleIdxMat = tripleIdxMatOrigin.copy()
        # Gradient descent on Logistic hypothesis
        print 'start gradient descent...'
        tic = time.clock()
        global tmpEntityMat
        tmpEntityMat = entityMat.copy()
        try:
            for itrRound in range( ITERATION_TIME + 1 ):
                # first update entityMat
                for row in range( entityMat.shape[0] ):
                    tmpEntityMat[ row ] = entityMat[ row ] - ALPHA *  derivativeToE( row )
                # then update relationTensor
                sumRT = 0
                for block in range( relationTensor.shape[0] ):
                    tmpD =  BETA * derivativeToR( block )
                    sumRT = sumRT + abs(tmpD).sum()
                    relationTensor[ block ] = relationTensor[ block ] - tmpD
                if itrRound % 20 == 0:
                    print '---Round:' + str(itrRound)
                    print 'delta for entity matrix:' + str( abs(entityMat - tmpEntityMat).sum())
                    print 'delta for relation tensor:' + str(sumRT)
                if computeLossFunction == 't' and itrRound % 100 == 0:
                    Loss = computeLoss()
                    print 'loss function:' + str(Loss)

                entityMat = tmpEntityMat.copy()
            toc = time.clock()
            print 'finish training. Time elapsed:' + str( toc - tic )

            print 'write result to file...'
            fout = open('result/result' + fileName + '_i' + str( ITERATION_TIME ) + '_d' + str( DIM ) + '.txt', 'w')
            fout.write('entity Matrix:\n' )
            for row in range( entityMat.shape[0] ):
                tmp_entity_vec = entityMat[ row ]
                for row in range(DIM):
                    fout.write( str(tmp_entity_vec[ 0, row ]) + '\t') 
                #for ele in entityMat[ row ]:
                 #   fout.write( str(ele) + '\t' )
                fout.write('\n')
            fout.write('relation Tensor:\n')
            for block in range( relationTensor.shape[ 0 ] ):
                tmp_relation_mat = relationTensor[ block ]
                for row in range(DIM):
                    for col in range(DIM):
                        fout.write( str(tmp_relation_mat[ row, col ]) + '\t' )
                    fout.write('\n')
                fout.write('-------------------------\n')
            fout.close()
            print 'write finished.'
        except Exception, e:
            print 'exception happened...', e

if __name__ == '__main__':
    main()
