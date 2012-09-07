package inc;

import common.gm_vocabulary;

public abstract class BackendGenerator extends gm_code_generator implements gm_backend {
	
	protected boolean _voca_created;
	protected gm_vocabulary _lang_voca = new gm_vocabulary();
	
	public BackendGenerator() {
		super();
		_voca_created = false;
	}
	
	public gm_vocabulary get_language_voca()
	{
		if (!_voca_created)
		{
			_voca_created = true;
			build_up_language_voca();
		}
		return _lang_voca;
	}
	
	protected void build_up_language_voca() {
	}
	
}
