" Quit when a syntax file was already loaded.
if exists('b:current_syntax') | finish|  endif

syntax match qdlVar "\k\+" nextgroup=qdlAssignment
"syntax match qdlAssignment ":\=" contained nextgroup=qdlValue
"syntax match qdlValue ".*" contained
syntax match qdlString "\'.*\'"
syntax match qdlComment "//.*"
syntax match qdlComment "\/\*(\*(?!\/)|[^*])*\*\/"
syntax match qdlNumber "/[+\-]?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+\-]?\d+)?/"



syntax match qdlOperator "\v\*"
syntax match qdlOperator "\v/"
syntax match qdlOperator "\v\+"
syntax match qdlOperator "\v-"
syntax match qdlOperator "\v\~"
syntax match qdlOperator "\v:\="
syntax match qdlOperator "\v\*\="
syntax match qdlOperator "\v/\="
syntax match qdlOperator "\v\+\="
syntax match qdlOperator "\v-\="

syntax match qdlOperator "\vπ"
syntax match qdlOperator "\v⦰"
syntax match qdlOperator "\v⌆"
syntax match qdlOperator "\v⊕"
syntax match qdlOperator "\v⊙"
syntax match qdlOperator "\v⌈"
syntax match qdlOperator "\v⌊"
syntax match qdlOperator "\v∅"
syntax match qdlOperator "\v∆"
syntax match qdlOperator "\v\\/"
syntax match qdlOperator "\v/\\"

syntax match qdlOperator "\v∩"
syntax match qdlOperator "\v∪"
syntax match qdlOperator "\v∈"
syntax match qdlOperator "\v∉"
syntax match qdlOperator "\v∋"
syntax match qdlOperator "\v∌"
syntax match qdlOperator "\v∃"
syntax match qdlOperator "\v∄"
syntax match qdlOperator "\v⟦"
syntax match qdlOperator "\v⟧"
syntax match qdlOperator "\v⊢"
syntax match qdlOperator "\v\|\^"
syntax match qdlOperator "\v→"
syntax match qdlOperator "\v×"
syntax match qdlOperator "\v÷"
syntax match qdlOperator "\v≤"
syntax match qdlOperator "\v≥"
syntax match qdlOperator "\v≡"
syntax match qdlOperator "\v≠"
syntax match qdlOperator "\v≈"
syntax match qdlOperator "\v¬"
syntax match qdlOperator "\v∧"
syntax match qdlOperator "\v∨"
syntax match qdlOperator "\v≔"
syntax match qdlOperator "\v≕"
syntax match qdlOperator "\v⊨"
syntax match qdlOperator "\v¯"
syntax match qdlOperator "\v⁺"
syntax match qdlOperator "\v⊗"



syntax keyword qdlKeyword abs
syntax keyword qdlKeyword acos
syntax keyword qdlKeyword acosh
syntax keyword qdlKeyword append
syntax keyword qdlKeyword asin
syntax keyword qdlKeyword asinh
syntax keyword qdlKeyword args
syntax keyword qdlKeyword axis
syntax keyword qdlKeyword atan
syntax keyword qdlKeyword atanh
syntax keyword qdlKeyword box
syntax keyword qdlKeyword break
syntax keyword qdlKeyword cb_exists
syntax keyword qdlKeyword cb_read
syntax keyword qdlKeyword cb_write
syntax keyword qdlKeyword ceiling
syntax keyword qdlKeyword check_after
syntax keyword qdlKeyword check_syntax
syntax keyword qdlKeyword common_keys
syntax keyword qdlKeyword constants
syntax keyword qdlKeyword contains
syntax keyword qdlKeyword continue
syntax keyword qdlKeyword cos
syntax keyword qdlKeyword cosh
syntax keyword qdlKeyword date_iso
syntax keyword qdlKeyword date_ms
syntax keyword qdlKeyword debug
syntax keyword qdlKeyword decode_b64
syntax keyword qdlKeyword detokenize
syntax keyword qdlKeyword differ_at
syntax keyword qdlKeyword diff
syntax keyword qdlKeyword dim
syntax keyword qdlKeyword dir
syntax keyword qdlKeyword display
syntax keyword qdlKeyword encode_b64
syntax keyword qdlKeyword exclude_keys
syntax keyword qdlKeyword execute
syntax keyword qdlKeyword exp
syntax keyword qdlKeyword expand
syntax keyword qdlKeyword file_read
syntax keyword qdlKeyword file_write
syntax keyword qdlKeyword floor
syntax keyword qdlKeyword fork
syntax keyword qdlKeyword for_each
syntax keyword qdlKeyword for_keys
syntax keyword qdlKeyword for_next
syntax keyword qdlKeyword from_hex
syntax keyword qdlKeyword from_json
syntax keyword qdlKeyword from_uri
syntax keyword qdlKeyword halt
syntax keyword qdlKeyword has_key
syntax keyword qdlKeyword has_value
syntax keyword qdlKeyword hash
syntax keyword qdlKeyword head
syntax keyword qdlKeyword  i
syntax keyword qdlKeyword identity
syntax keyword qdlKeyword include_keys
syntax keyword qdlKeyword index_of
syntax keyword qdlKeyword indices
syntax keyword qdlKeyword info
syntax keyword qdlKeyword input_form
syntax keyword qdlKeyword insert
syntax keyword qdlKeyword insert_at
syntax keyword qdlKeyword is_defined
syntax keyword qdlKeyword is_function
syntax keyword qdlKeyword is_list
syntax keyword qdlKeyword join
syntax keyword qdlKeyword keys
syntax keyword qdlKeyword list_copy
syntax keyword qdlKeyword list_keys
syntax keyword qdlKeyword list_starts_with
syntax keyword qdlKeyword list_subset
syntax keyword qdlKeyword ln
syntax keyword qdlKeyword log
syntax keyword qdlKeyword log_entry
syntax keyword qdlKeyword mask
syntax keyword qdlKeyword mod
syntax keyword qdlKeyword module_import
syntax keyword qdlKeyword module_load
syntax keyword qdlKeyword module_path
syntax keyword qdlKeyword n
syntax keyword qdlKeyword nroot
syntax keyword qdlKeyword numeric_digits
syntax keyword qdlKeyword os_env
syntax keyword qdlKeyword pi
syntax keyword qdlKeyword print
syntax keyword qdlKeyword query
syntax keyword qdlKeyword raise_error
syntax keyword qdlKeyword random
syntax keyword qdlKeyword random_string
syntax keyword qdlKeyword rank
syntax keyword qdlKeyword reduce
syntax keyword qdlKeyword remap
syntax keyword qdlKeyword remove
syntax keyword qdlKeyword rename_keys
syntax keyword qdlKeyword replace
syntax keyword qdlKeyword return
syntax keyword qdlKeyword reverse
syntax keyword qdlKeyword say
syntax keyword qdlKeyword scan
syntax keyword qdlKeyword script_args
syntax keyword qdlKeyword script_load
syntax keyword qdlKeyword script_path
syntax keyword qdlKeyword script_run
syntax keyword qdlKeyword set_default
syntax keyword qdlKeyword shuffle
syntax keyword qdlKeyword sin
syntax keyword qdlKeyword sinh
syntax keyword qdlKeyword size
syntax keyword qdlKeyword starts_with
syntax keyword qdlKeyword subset
syntax keyword qdlKeyword substring
syntax keyword qdlKeyword tail
syntax keyword qdlKeyword tan
syntax keyword qdlKeyword tanh
syntax keyword qdlKeyword to_boolean
syntax keyword qdlKeyword to_hex
syntax keyword qdlKeyword to_json
syntax keyword qdlKeyword to_list
syntax keyword qdlKeyword to_lower
syntax keyword qdlKeyword to_number
syntax keyword qdlKeyword to_string
syntax keyword qdlKeyword to_set
syntax keyword qdlKeyword to_upper
syntax keyword qdlKeyword to_uri
syntax keyword qdlKeyword tokenize
syntax keyword qdlKeyword transpose
syntax keyword qdlKeyword trim
syntax keyword qdlKeyword unbox
syntax keyword qdlKeyword union
syntax keyword qdlKeyword unique
syntax keyword qdlKeyword var_type
syntax keyword qdlKeyword vdecode
syntax keyword qdlKeyword vencode
syntax keyword qdlKeyword vfs_mount
syntax keyword qdlKeyword vfs_unmount
syntax keyword qdlKeyword ws_macro

highlight link qdlOperator Operator

hi def link qdlVar Identifier
hi def link qdlAssignment Statement
hi def link qdlString String
hi def link qdlComment Comment

let b:current_syntax = 'qdl'
