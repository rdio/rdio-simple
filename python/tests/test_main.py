# -*- coding: utf-8 -*-

from __future__ import unicode_literals

import unittest

try:
    from unittest.mock import Mock, patch
except ImportError:
    from mock import Mock, patch

from rdio import Rdio


sample_response = Mock()
sample_response.read.return_value = '{}'.encode('utf-8')

mock_urlopen = Mock()
mock_urlopen.return_value = sample_response


class MainTest(unittest.TestCase):

    def setUp(self):
        self.consumer = ('test', 'test')
        self.rdio = Rdio(self.consumer)

    @patch('rdio.urlopen', mock_urlopen)
    def test_unicode_params(self):
        r = self.rdio.call('search', {
            'query': 'Röyksopp'
        })
