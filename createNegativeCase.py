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

fread = open("dat/" + fileName + "-intermediate/triple.idx", "r")
existSet = set() 
for line in fread.readlines():
	line_list = line.split('\t')
	if len( line_list)  < 2:
		continue
	tmp_tuple = eval( '(' + line_list[0] + ',' + line_list[1] + ',' + line_list[2] + ')' )
	existSet.add( tmp_tuple )

fread.close()


fappend = open( "dat/" + fileName + "-intermediate/triple.idx" , 'a' )
negativeSet = set()
print "start generating random negative examples..."
while len( negativeSet ) < len( existSet ):
	if len( negativeSet ) % 1000 == 0:           # Display negative set size
		print len( negativeSet ) 
	rand_num1 = int( round( random.uniform( 1, ENTITY_SIZE  )) )
	rand_num2 = int( round( random.uniform( 1, ENTITY_SIZE  )) )
	rand_num3 = int( round( random.uniform( 1, RELATION_SIZE  )) )
	tmp_tuple = eval( '(' + str( rand_num1) + ',' + str( rand_num3 ) + ',' + str( rand_num2 ) + ')' )
	if (tmp_tuple in  existSet) or (tmp_tuple in  negativeSet)  :
		continue
	
	fappend.write( str( rand_num1 ) + '\t' + str( rand_num3 ) + '\t' + str( rand_num2 ) + '\t' + '0\n')
	negativeSet.add(tmp_tuple)
fappend.close()
