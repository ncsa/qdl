" Quit when a syntax file was already loaded.
if exists('b:current_syntax') | finish|  endif

syntax match qdlVar "\k\+" nextgroup=qdlAssignment
"syntax match qdlAssignment ":\=" contained nextgroup=qdlValue
"syntax match qdlValue ".*" contained
syntax match qdlString "\'.*\'"
syntax match qdlComment "//.*"
syntax match qdlComment "===.*"
syntax match qdlComment "».*"
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
syntax match qdlOperator "\vµ"
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
syntax match qdlOperator "\v∂"


syntax keyword qdlFunction abs
syntax keyword qdlFunction acos
syntax keyword qdlFunction acosh
syntax keyword qdlFunction apply
syntax keyword qdlFunction arg_count
syntax keyword qdlFunction args
syntax keyword qdlFunction asin
syntax keyword qdlFunction asinh
syntax keyword qdlFunction atan
syntax keyword qdlFunction atanh
syntax keyword qdlFunction box
syntax keyword qdlFunction break
syntax keyword qdlFunction cb_exists
syntax keyword qdlFunction cb_read
syntax keyword qdlFunction cb_write
syntax keyword qdlFunction ceiling
syntax keyword qdlFunction check_after
syntax keyword qdlFunction check_syntax
syntax keyword qdlFunction common_keys
syntax keyword qdlFunction constants
syntax keyword qdlFunction contains
syntax keyword qdlFunction continue
syntax keyword qdlFunction cos
syntax keyword qdlFunction cosh
syntax keyword qdlFunction date_iso
syntax keyword qdlFunction date_ms
syntax keyword qdlFunction debugger
syntax keyword qdlFunction decode
syntax keyword qdlFunction detokenize
syntax keyword qdlFunction diff
syntax keyword qdlFunction differ_at
syntax keyword qdlFunction dim
syntax keyword qdlFunction dir
syntax keyword qdlFunction docs
syntax keyword qdlFunction encode
syntax keyword qdlFunction excise
syntax keyword qdlFunction exclude_keys
syntax keyword qdlFunction exp
syntax keyword qdlFunction expand
syntax keyword qdlFunction file_read
syntax keyword qdlFunction file_write
syntax keyword qdlFunction floor
syntax keyword qdlFunction for_each
syntax keyword qdlFunction for_keys
syntax keyword qdlFunction for_lines
syntax keyword qdlFunction for_next
syntax keyword qdlFunction fork
syntax keyword qdlFunction from_json
syntax keyword qdlFunction from_uri
syntax keyword qdlFunction funcs
syntax keyword qdlFunction gcd
syntax keyword qdlFunction halt
syntax keyword qdlFunction has_key
syntax keyword qdlFunction has_keys
syntax keyword qdlFunction has_value
syntax keyword qdlFunction hash
syntax keyword qdlFunction head
syntax keyword qdlFunction i
syntax keyword qdlFunction identity
syntax keyword qdlFunction import
syntax keyword qdlFunction include_keys
syntax keyword qdlFunction index_of
syntax keyword qdlFunction indices
syntax keyword qdlFunction info
syntax keyword qdlFunction input_form
syntax keyword qdlFunction insert
syntax keyword qdlFunction insert_at
syntax keyword qdlFunction interpret
syntax keyword qdlFunction is_defined
syntax keyword qdlFunction is_function
syntax keyword qdlFunction is_list
syntax keyword qdlFunction is_null
syntax keyword qdlFunction j_load
syntax keyword qdlFunction j_use
syntax keyword qdlFunction join
syntax keyword qdlFunction keys
syntax keyword qdlFunction kill
syntax keyword qdlFunction lcm
syntax keyword qdlFunction lib_entries
syntax keyword qdlFunction list_copy
syntax keyword qdlFunction list_keys
syntax keyword qdlFunction ln
syntax keyword qdlFunction load
syntax keyword qdlFunction loaded
syntax keyword qdlFunction log
syntax keyword qdlFunction logger
syntax keyword qdlFunction mask
syntax keyword qdlFunction max
syntax keyword qdlFunction min
syntax keyword qdlFunction mod
syntax keyword qdlFunction module_import
syntax keyword qdlFunction module_load
syntax keyword qdlFunction module_path
syntax keyword qdlFunction module_remove
syntax keyword qdlFunction n
syntax keyword qdlFunction names
syntax keyword qdlFunction nroot
syntax keyword qdlFunction numeric_digits
syntax keyword qdlFunction os_env
syntax keyword qdlFunction pi
syntax keyword qdlFunction pick
syntax keyword qdlFunction print
syntax keyword qdlFunction query
syntax keyword qdlFunction raise_error
syntax keyword qdlFunction random
syntax keyword qdlFunction random_string
syntax keyword qdlFunction rank
syntax keyword qdlFunction reduce
syntax keyword qdlFunction remap
syntax keyword qdlFunction remove
syntax keyword qdlFunction rename
syntax keyword qdlFunction rename_keys
syntax keyword qdlFunction replace
syntax keyword qdlFunction return
syntax keyword qdlFunction reverse
syntax keyword qdlFunction say
syntax keyword qdlFunction scan
syntax keyword qdlFunction script_args
syntax keyword qdlFunction script_load
syntax keyword qdlFunction script_name
syntax keyword qdlFunction script_path
syntax keyword qdlFunction script_run
syntax keyword qdlFunction set_default
syntax keyword qdlFunction shuffle
syntax keyword qdlFunction sin
syntax keyword qdlFunction sinh
syntax keyword qdlFunction size
syntax keyword qdlFunction sleep
syntax keyword qdlFunction sort
syntax keyword qdlFunction star
syntax keyword qdlFunction starts_with
syntax keyword qdlFunction sublist
syntax keyword qdlFunction substring
syntax keyword qdlFunction tail
syntax keyword qdlFunction tan
syntax keyword qdlFunction tanh
syntax keyword qdlFunction to_boolean
syntax keyword qdlFunction to_json
syntax keyword qdlFunction to_lower
syntax keyword qdlFunction to_number
syntax keyword qdlFunction to_string
syntax keyword qdlFunction to_upper
syntax keyword qdlFunction to_uri
syntax keyword qdlFunction tokenize
syntax keyword qdlFunction transpose
syntax keyword qdlFunction trim
syntax keyword qdlFunction unbox
syntax keyword qdlFunction union
syntax keyword qdlFunction unique
syntax keyword qdlFunction unload
syntax keyword qdlFunction use
syntax keyword qdlFunction values
syntax keyword qdlFunction var_type
syntax keyword qdlFunction vars
syntax keyword qdlFunction vfs_mount
syntax keyword qdlFunction vfs_unmount
syntax keyword qdlFunction ws_macro

syntax keyword qdlReserved assert
syntax keyword qdlReserved block
syntax keyword qdlReserved body
syntax keyword qdlReserved catch
syntax keyword qdlReserved define
syntax keyword qdlReserved do
syntax keyword qdlReserved else
syntax keyword qdlReserved false
syntax keyword qdlReserved if
syntax keyword qdlReserved local
syntax keyword qdlReserved module
syntax keyword qdlReserved null
syntax keyword qdlReserved switch
syntax keyword qdlReserved then
syntax keyword qdlReserved true
syntax keyword qdlReserved try
syntax keyword qdlReserved while
syntax keyword qdlReserved ⊨
syntax keyword qdlReserved ∅


highlight link qdlOperator Operator

hi def link qdlVar Identifier
hi def link qdlNumber Number
hi def link qdlAssignment Statement
hi def link qdlString String
hi def link qdlComment Comment
hi def link qdlReserved Type
hi def link qdlFunction Keyword


let b:current_syntax = 'qdl'
