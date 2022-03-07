# MitochondrialFlow_
This is a Java-based plugin for quantification of mitochondrial characteristics operational under either ImageJ or Fiji. 
## Table of Contents  
- [Overview of Procedure](#overview-of-procedure)
- [Installation](#installation)
- [References](#references)
-
<a name="overview-of-procedure"></a>
## Overview of Procedure
Analysis of mitochondrial morphology was performed using Java-based “MitochondrialFlow” plugin operational under both ImageJ and Fiji 25. With the following protocol, the images were first cropped to show a single cell per image. Once done, “MitochondrialFlow” plugin was runned on Fiji using cropped images as input. The image was first processed through ImageJ’s “UnsharpMask” command for both sharpening and enhancing edges then, “Enhance Local Contrast” command was applied for enhancing the local contrast of image and whether checked, ImageJ’s “Mean” filtering command for smoothing may be applied. Thereafter, image was thresholded (see above) and ImageJ’s “AnalyzeParticles” command was runned to perform particle analysis on specified binary image measuring for “Area”, “Perimeter”, and “Shape Descriptors”. Form Factor (FF) was derived as the inverse of the “Circularity” output value. For network connectivity analysis, “Skeletonize” algorithm was applied to binary image  generating a skeleton map through removing pixels from edges of objects. Subsequently, “AnalyzeSkeleton” class was used for skeleton analysis calculating number of junctions, triple and quadruple points and branches, and measuring branch average and maximum length in the skeletonized network. Finally, automatically, “MitochondrialFlow” may establish correspondence relationship among binarized and skeletonized objects being able to export results for each image related to both particle and skeleton analysis.


<p align="center">
  <img width="800" height="450" src="https://user-images.githubusercontent.com/83207172/157086168-08937cb3-10f3-4812-b29b-7e77489db918.png">
</p>

<a name="installation"></a>
## Installation

The ***MitochondrialAnalyzer*** plugin may be installed in Fiji or ImageJ by following these steps:

1. In the event of not having ImageJ or Fiji already installed, please navigate through [https://imagej.nih.gov/ij/download.html](https://imagej.nih.gov/ij/download.html), download it and then, install it on a computer with Java pre-installed in either Windows, Mac OS or Linux systems.
2.  Once done, download the plugin JAR file named as [MitochondrialFlow_.jar](https://github.com/QuantitativeImageAnalysisUnitCNB/MitochondrialAnalyzer_/blob/master/MitochondrialAnalyzer_.jar) from repository.
3.  Move this file into the `ImageJ/Fiji "plugins" subfolder`, or differently, by dragging and dropping into the `ImageJ/Fiji main window` or optionally, running through menu bar `"Plugins"` **→** `"Install"` **→**  `‘Path to File’`. Then restart either ImageJ or Fiji and it is about time to start using "MitochondrialAnalyzer".
<a name="references"></a>
## References
<a id="1">[1]</a> 
Arganda-Carreras, I., Fernández-González, R., Muñoz-Barrutia, A., & Ortiz-De-Solorzano, C. (2010). 
3D reconstruction of histological sections: Application to mammary gland tissue. 
Microscopy Research and Technique, 73(11), 1019–1029. [![DOI:10.1038/nmeth.2019](http://img.shields.io/badge/DOI-10.1101/2021.01.08.425840-B31B1B.svg)](https://doi.org/10.1002/jemt.20829)


