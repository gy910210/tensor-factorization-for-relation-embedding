import random

switch = raw_input('enter 30 for fb30, 15k for fb15k:')
if switch == '30':
    fileName = 'fb30'
    ENTITY_SIZE = 58
    RELATION_SIZE = 24
elif switch == '15k':
    fileName = 'fb15k'
    ENTITY_SIZE = 14505
    RELATION_SIZE = 237
else:
    print "no dataset found"
    exit(0)

random.seed(1)

# First fill the Positive set so that we know if a random generated 
# triple really not exists
posSet = set()
fread = open("dat/" + fileName + "-intermediate/triple.idx", "r")
for line in fread.readlines():
    lineSplit = line.split()
    posSet.add( eval('(' + lineSplit[0] + ',' + lineSplit[1] + ',' + lineSplit[2] + ')' ))
fread.close()

# Generate negative examples
fread = open("dat/" + fileName + "-intermediate/triple.idx", "r")
fneg = open( "dat/" + fileName + "-intermediate/triple_negative.idx", 'w' )
generateCnter = 0
negSet = set()
for line in fread.readlines():
    lineSplit = line.split()
    trilogy = random.randint( 1, 3 )
    e1 = lineSplit[0]
    r = lineSplit[1]
    e2 = lineSplit[2]
    if trilogy == 1: # fix e1, r, rand e2
       while generateCnter < 5:
           tmp = random.randint( 1,ENTITY_SIZE  )
           tmp_tuple = eval('(' + e1 + ',' + r + ',' + str( tmp ) + ')' )
           if tmp_tuple not in  posSet and tmp_tuple not in negSet:
                fneg.write(e1 + '\t' + r + '\t' + str(tmp) + '\n' )
                generateCnter = generateCnter + 1
                negSet.add(tmp_tuple)
    elif trilogy == 2:
         while generateCnter < 5:
           tmp = random.randint( 1, RELATION_SIZE)
           tmp_tuple = eval('(' + e1 + ',' + str(tmp) + ',' + e2 + ')' )
           if tmp_tuple not in  posSet and tmp_tuple not in negSet:
               fneg.write(e1 + '\t' + str(tmp) + '\t' + e2 + '\n')
               negSet.add(tmp_tuple)
               generateCnter = generateCnter + 1
    elif trilogy == 3:
         while generateCnter < 5:
           tmp = random.randint( 1, ENTITY_SIZE)
           tmp_tuple = eval('(' + e1 + ',' + r + ',' + str(tmp) + ')' )
           if tmp_tuple not in posSet and tmp_tuple not in negSet:
                fneg.write( e1 + '\t' + r + '\t' + str(tmp)  + '\n' )
                generateCnter = generateCnter + 1
                negSet.add(tmp_tuple)
    generateCnter = 0

fread.close()
fneg.close()
