all: result/documentation.pdf


result/documentation.pdf: *.tex
	mkdir -p result
	pdflatex documentation.tex
	pdflatex documentation.tex
	mv documentation.pdf result
	rm documentation.toc documentation.out documentation.log documentation.aux

clean:
	rm -r result