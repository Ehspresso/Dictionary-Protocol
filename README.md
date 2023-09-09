# Dictionary-Protocol
A program which implements the Dictionary server protocol (as specificed in the RFC 2229).
Written in java.

A brief introduction to the protocol, provided by RFC:

Introduction

   For many years, the Internet community has relied on the "webster"
   protocol for access to natural language definitions.  The webster
   protocol supports access to a single dictionary and (optionally) to a
   single thesaurus.  In recent years, the number of publicly available
   webster servers on the Internet has dramatically decreased.

   Fortunately, several freely-distributable dictionaries and lexicons
   have recently become available on the Internet.  However, these
   freely-distributable databases are not accessible via a uniform
   interface, and are not accessible from a single site.  They are often
   small and incomplete individually, but would collectively provide an
   interesting and useful database of English words.  Examples include
   the Jargon file [JARGON], the WordNet database [WORDNET], MICRA's
   version of the 1913 Webster's Revised Unabridged Dictionary
   [WEB1913], and the Free Online Dictionary of Computing [FOLDOC].
   Translating and non-English dictionaries are also becoming available
   (for example, the FOLDOC dictionary is being translated into
   Spanish).

   The webster protocol is not suitable for providing access to a large
   number of separate dictionary databases, and extensions to the
   current webster protocol were not felt to be a clean solution to the
   dictionary database problem.

   The DICT protocol is designed to provide access to multiple
   databases.  Word definitions can be requested, the word index can be
   searched (using an easily extended set of algorithms), information
   about the server can be provided (e.g., which index search strategies
   are supported, or which databases are available), and information
   about a database can be provided (e.g., copyright, citation, or
   distribution information).  Further, the DICT protocol has hooks that
   can be used to restrict access to some or all of the databases.
