import numpy as np
import time
np.random.seed(1)

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
        return 1 / ( 1 + np.exp(product) )


# Read triple idx
# return matrix of triple with annotation
def readTripleToMat( ):
    fid =  open( 'dat/' + fileName + '-intermediate/triple.idx', 'r')
    mat = np.asmatrix([0,0,0,0])
    for line in fid.readlines():
        lineSplit = line.split('\t')
        newrow = [ int( lineSplit[0] ), int( lineSplit[1] ), int( lineSplit[2] ),  int( lineSplit[3] )]
        mat = np.vstack([ mat, newrow ])

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
    if leftDict.has_key( i + 1 ): 
        for k in leftDict[ i + 1 ]:
            tmp_triple = tripleIdxMat[ k ] # since the first row is not used, the triple idx matrix starts from 1
            tmp_rel = relationTensor[ tmp_triple[ 0, 1 ] - 1 ] # matrix for rm
            tmp_right = entityMat[ tmp_triple[ 0, 2 ] - 1 ]  # matrix for ej
            rst = rst + ( tmp_triple[ 0, 3 ] - logistic( tmp_this, tmp_rel, tmp_right ) ) * (( tmp_rel.dot( tmp_right.transpose() ) ).transpose()) 
    if rightDict.has_key( i + 1 ):
        for k in rightDict[ i + 1 ]:
            tmp_triple = tripleIdxMat[ k ] # since the first row is not used, the triple idx matrix starts from 1
            tmp_rel = relationTensor[ tmp_triple[ 0, 1 ] - 1 ] # matrix for rm
            tmp_left = entityMat[ tmp_triple[ 0, 0 ] - 1 ]  # matrix for ej
            rst = rst + ( tmp_triple[ 0, 3 ] - logistic( tmp_left, tmp_rel, tmp_this ) ) * ( tmp_this.dot(tmp_rel) ) 
    return rst


# Compute the derivative of loss function with respect to relation matrix rm
def derivativeToR( block ):
    rst = np.zeros([DIM, DIM])
    tmp_this = relationTensor[ block ]
    for k in relDict[ block + 1 ]:
        tmp_triple = tripleIdxMat[ k ] # since the first row is not used, the triple idx matrix starts from 1
        tmp_left = entityMat[ tmp_triple[ 0, 0 ] - 1 ] # vector for ei
        tmp_right = entityMat[ tmp_triple[ 0, 2 ] - 1 ]  # vector for ej
        rst = rst + ( tmp_triple[ 0, 3 ] - logistic( tmp_left, tmp_this, tmp_right ) ) * ( tmp_left.transpose() ).dot( tmp_right ) 

    return rst

def main():
    switch = raw_input('enter 30 for fb30, 15k for fb15k, q for quit:')
    global fileName
    global ITERATION_TIME
    global DIM
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
    print 'start loading triple matrix(This is gonna take long) ...'
    tic = time.clock()
    global tripleIdxMatOrigin
    tripleIdxMatOrigin = readTripleToMat()
    toc = time.clock()
    print 'triple idx loaded. Time elapsed: ' + str( toc - tic )
    while True:
        switch = raw_input('enter q to quit, c to continue:')
        if switch == 'q':
            print "exit!"
            exit(0)

        DIM = int(raw_input('enter vector dimension:'))
        ITERATION_TIME = int(raw_input('enter iteration time:'))
        # Initialize matrix and tensor
        global entityMat
        global relationTensor
        entityMat = np.asmatrix( np.random.rand( ENTITY_SIZE, DIM ) )
        relationTensor =  np.random.rand( RELATION_SIZE, DIM, DIM )
        
        global tripleIdxMat        
        tripleIdxMat = tripleIdxMatOrigin.copy()
        
        print 'start loading inc_left.list...'
        fidLeft = open('dat/'+ fileName +'-intermediate/inc_left.list', 'r')
        global leftDict
        leftDict = readIncToDic( fidLeft )
        fidLeft.close()
        print 'start loading inc_left.list...'
        fidRight = open('dat/' + fileName + '-intermediate/inc_right.list', 'r')
        global rightDict
        rightDict = readIncToDic( fidRight )
        fidRight.close()

        print 'start loading inc_left.list...'
        fidRel = open('dat/' + fileName + '-intermediate/inc_rel.list', 'r')
        global relDict
        relDict = readIncToDic( fidRel )
        fidRel.close()

        # Gradient descent on Logistic hypothesis
        print 'start gradient descent...'
        tic = time.clock()
        global tmpEntityMat
        tmpEntityMat = entityMat.copy()
        for itrRound in range( ITERATION_TIME ):
            print '---Round:' + str(itrRound)
            # first update entityMat
            for row in range( entityMat.shape[0] ):
                tmpEntityMat[ row ] = entityMat[ row ] - ALPHA *  derivativeToE( row )
            # then update relationTensor
            sumRT = 0
            for block in range( relationTensor.shape[0] ):
                tmpD =  BETA * derivativeToR( block )
                sumRT = sumRT + abs(tmpD).sum()
                relationTensor[ block ] = relationTensor[ block ] - tmpD
            print 'delta for entity matrix:' + str( abs(entityMat - tmpEntityMat).sum())
            print 'delta for relation tensor:' + str(sumRT)
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


if __name__ == '__main__':
    main()
