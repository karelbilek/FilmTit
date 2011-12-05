from nltk.tokenize import sent_tokenize
import sys
import argparse

parser = argparse.ArgumentParser(description='Generate test sentences.')
parser.add_argument("n", help="number of sentences to generate", type=int)
args = parser.parse_args()

i = 0
for line in sys.stdin:
    sents = sent_tokenize(line)
    print "\n".join(map(str.strip, sents))
    i += len(sents)
    if i >= args.n:
        break

print >>sys.stderr, "Extracted %d sentences." % (i,)