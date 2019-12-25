import os
import argparse
import matplotlib.pyplot as plt

def plotQueryTime(unCompressionFile, compressionFile):
	x, yUncompress = [], []
	with open(unCompressionFile, 'r') as file:
		
		for timestamp in file.readlines():
			num, time = timestamp.split()

			x.append(int(num))
			yUncompress.append(float(time))

	yCompress = []
	with open(compressionFile, 'r') as file:
		
		for timestamp in file.readlines():
			num, time = timestamp.split()

			yCompress.append(float(time))

	plt.xticks([i for i in range(len(x))], x)
	plt.plot(yUncompress, label = 'uncompress', marker = 'o')
	plt.plot(yCompress, label = 'compress', marker = 'o')
	plt.xlabel('Number of 7 terms query')
	plt.ylabel('Time (second)')
	plt.legend()
	plt.title('Compresison hypothesis')

	if not os.path.exists('img/'):
		os.makedirs('img/')


	plt.savefig('img/compressionHypothesis.png')



if __name__ == '__main__':
	parser = argparse.ArgumentParser()
	parser.add_argument('-uc', help = 'the file path for the compression version query time', default = 'query/queryTimeUncompress.txt')
	parser.add_argument('-c', help = 'the file path for the uncompression version query time', default = 'query/queryTimeCompress.txt')
	args = parser.parse_args()

	plotQueryTime(args.uc, args.c)
