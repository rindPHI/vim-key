" Quit when a syntax file was already loaded.
if exists('b:current_syntax') | finish | endif

" Comments
hi def link keyComment Comment
hi def link keyMLComment Comment
syn match  keyComment "//.*$"
syn region keyMLComment start=/\/\*/ end=/\*\//

" Special Top-Level Directives
syn match  keyInclude "\\include"
hi def link keyInclude Include

" Predicates and functions declaration
syn match  keyPredsDecl /\\\(predicates\|functions\)\s*/ nextgroup=keyPredsBlock
syn region keyPredsBlock start=/{/ end=/}/ fold contained

hi def link keyPredsDecl Statement

" Sorts declaration
syn match  keySortsDecl /\\sorts\s*/ nextgroup=keySortsBlock
syn region keySortsBlock start=/{/ end=/}/ fold  contains=keySortDecl contained
syn match  keySortDecl /^\s*\S\+.*;/ contains=keySort,keyExtends contained
syn match  keySort /^\s*\S\+\s/ contained
syn match  keyExtends /\\extends/ contained

hi def link keySortsDecl Statement
hi def link keySort Type
hi def link keyExtends Statement

" Schema variables declaration
syn match  keySVsDecl /\\schemaVariables\s*/ nextgroup=keySVBlock
syn region keySVBlock start=/{/ end=/}/ fold  contains=keySVDecl,keyComment,keyMLComment contained
syn match keySVDecl /^\s*.*;/ contains=keySort contained

hi def link keySVsDecl Statement

" Rules declaration
syn match  keyRulesDecl /\\rules\s*/ nextgroup=keyRulesScope
syn match  keyRulesScope /([a-zA-Z]\+:[a-zA-Z]\+\(,\s*[a-zA-Z]\+:[a-zA-Z]\+\)*)\s*/ contained nextgroup=keyRulesBlock
syn region keyRulesBlock
  \ matchgroup=rulesBlockStart start=/\s*{/
  \ matchgroup=rulesBlockEnd end=/}\s*\n/
  \ fold contains=keyComment,keyMLComment,keyRuleDecl,keyRuleDeclBlock,keyRuleName contained
  \ 

syn region keyRuleDeclBlock start=/{/ end=/};/ contained contains=keyComment,keyMLComment,keyNestedBlock,keyRuleKeywords,keyString,keySVIdentifier fold 
syn region keyNestedBlock start=/([a-zA-Z]\+:[a-zA-Z]\+)\s*{/ end=/};/ contained transparent contains=keyInnerRulesScope,keyString,keySVIdentifier,keyRuleKeywords
syn match  keyInnerRulesScope /([a-zA-Z]\+:[a-zA-Z]\+\(,\s*[a-zA-Z]\+:[a-zA-Z]\+\)*)\s*/ contained
syn match  keyRuleName /[a-zA-Z0-9_]\+/ contained
syn match  keyString /"[^"]\{-}"/ contained
syn match  keySVIdentifier /#[a-zA-Z0-9]\+/ contained

hi def link keyRulesDecl Statement
hi def link keyRuleName Identifier
hi def link keyRulesScope Type
hi def link keyInnerRulesScope Type
hi def link keyString String
hi def link keySVIdentifier Identifier
" hi def link rulesBlockStart MatchParen
" hi def link rulesBlockEnd MatchParen

" Taclet keywords
syn match keyRuleKeywords /\\\(find\|assumes\|modality\|replacewith\|add\|heuristics\|endmodality\|varcond\|not\|hasSort\|isThisReference\|staticMethodReference\|displayname\|sameUpdateLevel\|schemaVar\|modalOperator\)/ contained

hi def link keyRuleKeywords Macro

let b:current_syntax = "key"

" Fold
set foldmethod=syntax
