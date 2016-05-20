/*
Copyright 2016 First People's Cultural Council

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
import React, {Component, PropTypes} from 'react';
import classNames from 'classnames';
import provide from 'react-redux-provide';
import selectn from 'selectn';
import t from 'tcomb-form';

import ProviderHelpers from 'common/ProviderHelpers';

// Views
import RaisedButton from 'material-ui/lib/raised-button';
import Paper from 'material-ui/lib/paper';
import CircularProgress from 'material-ui/lib/circular-progress';
import Snackbar from 'material-ui/lib/snackbar';

import fields from 'models/schemas/fields';
import options from 'models/schemas/options';

/**
* Create book entry
*/
@provide
export default class PageDialectStoriesAndSongsBookEntryCreate extends Component {

  static propTypes = {
    windowPath: PropTypes.string.isRequired,
    splitWindowPath: PropTypes.array.isRequired,
    pushWindowPath: PropTypes.func.isRequired,
    fetchDialect: PropTypes.func.isRequired,
    computeDialect: PropTypes.object.isRequired,
    fetchBook: PropTypes.func.isRequired,
    computeBook: PropTypes.object.isRequired,
    createBookEntry: PropTypes.func.isRequired,
    routeParams: PropTypes.object
  };

  constructor(props, context){
    super(props, context);

    this.state = {
      formValue: null,
      dialectPath: null,
      parentBookPath: null
    };

    // Bind methods to 'this'
    ['_onNavigateRequest', '_onRequestSaveForm'].forEach( (method => this[method] = this[method].bind(this)) );
  }

  fetchData(newProps) {

    let parentBookPath = newProps.routeParams.dialect_path + '/Stories & Songs/' + newProps.routeParams.parentBookName;

    newProps.fetchDialect(newProps.routeParams.dialect_path);
    newProps.fetchBook(parentBookPath);

    this.setState({
      dialectPath: newProps.routeParams.dialect_path,
      parentBookPath: parentBookPath
    });
  }

  // Fetch data on initial render
  componentDidMount() {
    this.fetchData(this.props);
  }

  // Refetch data on URL change
  componentWillReceiveProps(nextProps) {
    if (nextProps.windowPath !== this.props.windowPath) {
      this.fetchData(nextProps);
    }
  }

  shouldComponentUpdate(newProps, newState) {

    switch (true) {
      case (newProps.windowPath != this.props.windowPath):
        return true;
      break;

      case (newProps.computeDialect.response != this.props.computeDialect.response):
        return true;
      break;
      
      case (selectn('books[' + this.state.parentBookPath + '].response', newProps.computeBook) != selectn('books[' + this.state.parentBookPath + '].response', this.props.computeBook)):
        return true;
      break;
    }

    return false;
  }

  _onNavigateRequest(path) {
    //this.props.pushWindowPath('/' + path);
  }

  _onRequestSaveForm(e) {

    // Prevent default behaviour
    e.preventDefault();

    let formValue = this.refs["form_book_entry_create"].getValue();

    let properties = {};
    
	  for (let key in formValue) {
	    if (formValue.hasOwnProperty(key) && key) {
	      if (formValue[key] && formValue[key] != '') {
	    	  properties[key] = formValue[key];
	  	  }
	    }
	  }

    this.setState({
      formValue: properties
    })

    // Passed validation
    if (formValue) {
  	  this.props.createBookEntry(this.state.parentBookPath, {
  	    type: 'FVBookEntry',
  	    name: formValue['dc:title'],
  	    properties: properties
  	  });
    } else {
      window.scrollTo(0, 0);
    }

  }

  render() {

    let FVBookEntryOptions = Object.assign({}, selectn("FVBookEntry", options));

    const { computeBook, computeDialect } = this.props;

    let dialect = computeDialect.response;

    //let book = ProviderHelpers.getEntry(computeBook, this.state.parentBookPath);

    let book = selectn('books[' + this.state.parentBookPath + ']', computeBook);
    let bookResponse = selectn('response', book);

    if (computeDialect.isFetching || (bookResponse && bookResponse.isFetching)) {
      return <CircularProgress mode="indeterminate" size={2} />;
    }

    // Set default value on form
    if (selectn('response.properties.fvdialect:dominant_language', this.props.computeDialect)) {

      if (selectn("fields.fv:literal_translation.item.fields.language.attrs", FVBookEntryOptions)) {
        FVBookEntryOptions['fields']['fv:literal_translation']['item']['fields']['language']['attrs']['defaultValue'] = selectn('response.properties.fvdialect:dominant_language', this.props.computeDialect);
      }

      if (selectn("fields.fvbookentry:dominant_language_text.item.fields.language.attrs", FVBookEntryOptions)) {
        FVBookEntryOptions['fields']['fvbookentry:dominant_language_text']['item']['fields']['language']['attrs']['defaultValue'] = selectn('response.properties.fvdialect:dominant_language', this.props.computeDialect);
      }
    }

    return <div>

            <h1>Add New Entry to <i>{selectn('properties.dc:title', bookResponse)}</i> Book</h1>
            
            <div className="row" style={{marginTop: '15px'}}>

              <div className={classNames('col-xs-8', 'col-md-10')}>
                <form onSubmit={this._onRequestSaveForm}>
                  <t.form.Form
                    ref="form_book_entry_create"
                    type={t.struct(selectn("FVBookEntry", fields))}
                    context={dialect}
                    value={this.state.formValue}
                    options={FVBookEntryOptions} />
                    <div className="form-group">
                      <button type="submit" className="btn btn-primary">Save</button> 
                    </div>
                </form>
              </div>

              <div className={classNames('col-xs-4', 'col-md-2')}>

                <Paper style={{padding: '15px', margin: '20px 0'}} zDepth={2}>

                  <div className="subheader">Metadata</div>

                </Paper>

              </div>
          </div>
        </div>;
  }
}