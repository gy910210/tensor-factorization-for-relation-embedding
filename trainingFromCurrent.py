#! /usr/bin/python
import threading
import numpy as np
import time
import math
import sys
np.random.seed(20)

ALPHA = 0.0005
BETA = 0.0005


def main():

    # -- Input arguments --
    switch = raw_input('enter 30 for fb30, 15k for fb15k, q for quit:')
    global fileName
    global ITERATION_TIM,EDIM,TRIPLE_NUM,ENTITY_NUM,RELATION_NUM
    global computeLossFunction
    if switch == '30':
        ENTITY_NUM= 58
        RELATION_NUM = 24
        fileName = 'fb30'
        TRIPLE_NUM = 30 
    elif switch == '15k':
        ENTITY_NUM = 14505
        RELATION_NUM = 237
        TRIPLE_NUM = 272115 
        fileName = 'fb15k'
    elif switch == 'q':
        print "exit!"
        exit(0)
    else:
        print "no dataset"
        exit(0)

    global leftDict, rightDict, relDict, leftReady, rightReady, relReady, posTriReady, negTriReady
    leftReady = False
    rightReady = False
    relReady = False
    posTriReady = False
    negTriReady = False
    readLeftThd = threading.Thread(target = readIncLeft )
    readLeftThd.start()
    readRightThd = threading.Thread(target = readIncRight )
    readRightThd.start()
    readRelThd = threading.Thread(target = readIncRel )
    readRelThd.start()

    # -- Loading the triple matrix ( Positive and Negative ) --
    print 'start loading triple matrix(This is gonna take long) ...'
    global tripleIdxMatOrigin
    global entityMat, relationTensor
    tripleIdxMatOrigin = np.zeros([ 2 * TRIPLE_NUM ,4 ], dtype = int)
    triplePosThd = threading.Thread(target = readPosTripleToMat)
    triplePosThd.start()
    tripleNegThd = threading.Thread(target = readNegTripleToMat)
    tripleNegThd.start()
    while not(leftReady and rightReady and relReady and negTriReady and posTriReady):
        pass
    while True:
        switch = raw_input('enter q to quit, c to continue:')
        if switch == 'q':
            print "exit!"
            exit(0)

        DIM = int(raw_input('enter vector dimension:'))
        ITERATION_TIME = int(raw_input('enter iteration time:'))
        computeLossFunction = raw_input('enter t to computer loss function, f to skip:')
        # Initialize matrix and tensor
        scratch = raw_input(' enter previous iteraton time to continue training:')
        if scratch == '0':
            entityMat = np.asmatrix( np.random.rand( ENTITY_NUM, DIM ) )
            relationTensor =  np.random.rand( RELATION_NUM, DIM, DIM )
        else:
            entityMat = np.ones( ( ENTITY_NUM, DIM), dtype = float)
            relationTensor = np.ones((RELATION_NUM, DIM, DIM), dtype = float)
            readTrained(scratch)
        global tripleIdxMat
        tripleIdxMat = tripleIdxMatOrigin.copy()
        # Gradient descent on Logistic hypothesis
        print 'start gradient descent...'
        tic = time.clock()
        global tmpEntityMat
        tmpEntityMat = entityMat.copy()
            
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
        fout = open('result/trained_' + fileName + '_i' + str( ITERATION_TIME ) +'+' +scratch + '_d' + str( DIM ) + '.txt', 'w')
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


# e1 and e2 are supposed to be 1 x DIM vectors
# r1 is supposed to be DIM x DIM matrix
# return sigmoid function value
def logistic( e1, r1, e2 ):
    product = float ( e1 * r1 * e2.transpose() )
    return 1 / ( 1 + np.exp(-1 * product) )

def readTrained(iteration):
    global entityMat, relationTensor,DIM,ENTITY_NUM,RELATION_NUM
    fin = open('./result/trained_fb15k_i' + iteration + '+0_d5.txt','r')
    fin.readline()
    for i in range( ENTITY_NUM ):
        line = fin.readline()
        entityMat[ i ] = map( float, line.split())
    fin.readline()
    for j in range( RELATION_NUM ):
        tmp_relation_mat = np.ones( (DIM, DIM) , dtype = float)
        for k in range( DIM ):
            line = fin.readline()
            tmp_relation_mat[ k ] = map( float, line.split() )
        relationTensor[j] = tmp_relation_mat.copy()
        fin.readline()
    fin.close()
    entityMat = np.asmatrix( entityMat )
    return 


def readPosTripleToMat( ):
    print 'start reading positive triples...'
    posFid =  open( 'dat/' + fileName + '-intermediate/triple.idx', 'r')
    global TRIPLE_NUM, tripleIdxMatOrigin, posTriReady
    cnter = 0 

    for line in posFid.readlines():
        lineSplit = line.split()
        newrow = [ int( lineSplit[0] ), int( lineSplit[1] ), int( lineSplit[2] ),  1]
        tripleIdxMatOrigin[cnter] = np.array(newrow).copy()
        cnter += 1
    posTriReady = True
    print 'positive triples reading finished'


def readNegTripleToMat( ):
    print 'start reading negative triples...'
    negFid =  open( 'dat/' + fileName + '-intermediate/triple_negative.idx', 'r')
    global TRIPLE_NUM, tripleIdxMatOrigin, negTriReady
    cnter = TRIPLE_NUM

    for line in negFid.readlines():
        lineSplit = line.split()
        newrow = [ int( lineSplit[0] ), int( lineSplit[1] ), int( lineSplit[2] ),  1]
        tripleIdxMatOrigin[cnter] = np.array(newrow).copy()
        cnter += 1
    negTriReady = True
    print 'negative triples reading finished'


def readIncLeft( ):
    print 'start reading inc_left...'
    fid =  open('dat/'+ fileName +'-intermediate/inc_left.list', 'r')
    global leftDict, leftReady
    d = dict()
    for lines in fid.readlines():
        split1 = lines.split(':')
        split2 = split1[1].split('\t')
        d[ int( split1[ 0 ] ) ] = map(int, split2[:-1] )
    leftDict = d
    fid.close()
    leftReady = True
    print 'finish reading inc_left.'

def readIncRight( ):
    print 'start reading inc_right...'
    fid = open('dat/' + fileName + '-intermediate/inc_right.list', 'r')
    global rightDict, rightReady
    d = dict()
    for lines in fid.readlines():
        split1 = lines.split(':')
        split2 = split1[1].split('\t')
        d[ int( split1[ 0 ] ) ] = map(int, split2[:-1] )
    rightDict = d
    fid.close()
    rightReady = True
    print 'finish reading inc_right.'

def readIncRel( ):
    print 'start reading inc_rel...'
    fid =  open('dat/' + fileName + '-intermediate/inc_rel.list', 'r')
    global relDict, relReady
    d = dict()
    for lines in fid.readlines():
        split1 = lines.split(':')
        split2 = split1[1].split('\t')
        d[ int( split1[ 0 ] ) ] = map(int, split2[:-1] )
    relDict = d
    fid.close()
    relReady = True
    print 'finish reading inc_rel.'

# Compute the derivative of loss function with respect to Entity vector i
def derivativeToE( i ):
    rst = np.zeros([1, DIM])
    tmp_this = entityMat[ i ]
    if leftDict.has_key( i ):
        for k in leftDict[ i ]: # k is triple index
            tmp_triple = tripleIdxMat[ k ] # since the first row is not used, the triple idx matrix starts from 1
            tmp_rel = relationTensor[ tmp_triple[ 1 ]  ] # matrix for rm
            tmp_right = entityMat[ tmp_triple[ 2 ]  ]  # matrix for ej
            part0 = tmp_triple[ 3 ]
            part1 =  logistic( tmp_this, tmp_rel, tmp_right )
            part2 = tmp_right * tmp_rel.transpose()
            #part2 = ( tmp_rel.dot( tmp_right.transpose() ) ).transpose()
#            print 'LeftDict:\tpart0: ',part0,'\tpart1: ',part1,'\tpart2: ',part2
            rst +=  ( part0 - part1 ) * part2
    if rightDict.has_key( i ):
        for k in rightDict[ i ]:
            tmp_triple = tripleIdxMat[ k ]
            tmp_rel = relationTensor[ tmp_triple[ 1 ] ] # matrix for rm
            tmp_left = entityMat[ tmp_triple[ 0 ] ]  # matrix for ej
            part1 = logistic( tmp_left, tmp_rel, tmp_this )
            part2 = tmp_triple[ 3 ] - part1
#print 'RightDict:\tpart1: ',part1,'\tpart2: ',part2
            rst += part2 * ( tmp_left * tmp_rel )
    return -1 * rst

# Compute loss function
def computeLoss():
    rst = 0.0
    sampleSize = tripleIdxMat.shape[0]
    for i in range(0, sampleSize):
       currentRow = tripleIdxMat[ i ]
       y = currentRow[ 3 ]
       hx = logistic( entityMat[ currentRow[ 0 ] ], relationTensor[ currentRow[ 1 ] ], entityMat[ currentRow[ 2 ]] )
       rst += y * math.log( hx ) + ( 1 - y ) * math.log( 1 - hx )
    return -1 * rst


# Compute the derivative of loss function with respect to relation matrix rm
def derivativeToR( block ):
    rst = np.zeros([DIM, DIM])
    tmp_this = relationTensor[ block ]
    for k in relDict[ block ]:
        tmp_triple = tripleIdxMat[ k ]
        tmp_left = entityMat[ tmp_triple[ 0 ]  ] # vector for ei
        tmp_right = entityMat[ tmp_triple[ 2 ]  ]  # vector for ej
        rst += ( tmp_triple[ 3 ] - logistic( tmp_left, tmp_this, tmp_right ) ) * ( tmp_left.transpose() ).dot( tmp_right )

    return -1 * rst
if __name__ == '__main__':
    main()
