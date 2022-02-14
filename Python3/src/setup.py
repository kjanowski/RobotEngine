__author__="Kathrin Janowski, Stephan Kammerer"
__date__ ="$10.19.2018 19:15:00$"

from setuptools import setup,find_packages

setup (
  name = 'NaoEngine',
  version = '1.0.0',
  packages = find_packages(),

  # Declare your packages' dependencies here, for eg:
  #install_requires=['foo>=3'],

  # Fill in these to make your Egg ready for upload to PyPI
  author = 'Kathrin Janowski, Stephan Kammerer',
  author_email = '',

  summary = 'Nao implementation of the RobotEngine interface',
  url = '',
  license = '',
  long_description= 'Allows the Nao to receive and execute commands according to the RobotEngine messaging protocol.',

  # could also include long_description, download_url, classifiers, etc.

  
)
