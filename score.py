
import sys

print(str(sys.argv))
inF = sys.argv[1]
outF = sys.argv[2]
score  = 0.7
f1 =  open(outF, 'w')
f1.write(str(score))
f1.write('\n')
f1.close()




