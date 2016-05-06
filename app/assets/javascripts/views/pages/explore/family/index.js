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
import React, { Component, PropTypes } from 'react';
import provide from 'react-redux-provide';
import selectn from 'selectn';

// Operations
import DirectoryOperations from 'operations/DirectoryOperations';

import GridList from 'material-ui/lib/grid-list/grid-list';
import GridTile from 'material-ui/lib/grid-list/grid-tile';
import CircularProgress from 'material-ui/lib/circular-progress';

/**
* Explore Archive page shows all the families in the archive
*/
@provide
export default class ExploreFamily extends Component {

  static propTypes = {
    properties: PropTypes.object.isRequired,
    fetchLanguagesInPath: PropTypes.func.isRequired,
    computeLanguagesInPath: PropTypes.object.isRequired,
    fetchFamily: PropTypes.func.isRequired,
    computeFamily: PropTypes.object.isRequired,
    pushWindowPath: PropTypes.func.isRequired,
    windowPath: PropTypes.string.isRequired,
    splitWindowPath: PropTypes.array.isRequired,
    routeParams: PropTypes.object.isRequired
  };

  /*static contextTypes = {
      muiTheme: React.PropTypes.object.isRequired
  };*/

  constructor(props, context){
    super(props, context);

    // Bind methods to 'this'
    ['_onNavigateRequest'].forEach( (method => this[method] = this[method].bind(this)) );
  }

  componentDidMount() {
    this.props.fetchFamily(this.props.routeParams.language_family_path);
    this.props.fetchLanguagesInPath(this.props.routeParams.language_family_path);
  }

  _onNavigateRequest(path) {
    this.props.pushWindowPath('/explore' + path);
  }

  render() {

    const { computeLanguagesInPath, computeFamily } = this.props;

    if (computeLanguagesInPath.isFetching || computeFamily.isFetching) {
      return <CircularProgress mode="indeterminate" size={5} />;
    }

    let family = computeFamily.response;

    let languages = selectn('response.entries', computeLanguagesInPath) || [];

    return <div className="row">
            <div className="col-md-4 col-xs-12">
              <h1>{family.get('dc:title')}</h1>
              <div>
                <p>Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis.</p>
                <p>Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis.</p>
              </div>
            </div>
            <div className="col-md-8 col-xs-12">
                <h2>Browse the following Languages:</h2>
                <div style={{display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around'}}>
                  <GridList
                    cols={2}
                    cellHeight={200}
                    style={{width: '100%', height: 800, overflowY: 'auto', marginBottom: 24}}
                    >
                      {languages.map((tile, i) => 
                        <GridTile
                          onTouchTap={this._onNavigateRequest.bind(this, tile.path)}
                          key={tile.uid}
                          title={tile.title}
                          subtitle={tile.description}
                          ><img src="http://www.firstvoices.com/portal/tag1-1a.jpg" /></GridTile>
                      )}
                  </GridList>
                </div>
            </div>
          </div>;
  }
}